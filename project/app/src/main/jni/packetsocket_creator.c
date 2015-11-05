/* Creates a socket and passes its file descriptor via a Unix domain socket to another process. */

/* Needed for struct ucred */
#define _GNU_SOURCE

#include <arpa/inet.h>
#include <errno.h>
#include <inttypes.h>
#include <linux/un.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>

/* Arguments:
 * <caller pid> <abstract Unix domain socket path> <socket domain> <socket type> <socket protocol>
 */
int main(int argc, char **argv) {
    /* Unix domain socket */
    pid_t remote_pid;
    const char *abstract_path;
    size_t abstract_path_len;
    int uds, uds_listener;
    struct sockaddr_un bind_addr;
    struct ucred remote_cred;
    socklen_t remote_cred_len = sizeof remote_cred;

    /* Socket creation */
    int socket_fd, socket_domain, socket_type, socket_protocol;

    /* File descriptor passing */
    uint8_t dummy_message = 0;
    struct iovec iov = {.iov_base = &dummy_message, .iov_len = 1};
    unsigned char ancillary_data[CMSG_SPACE(sizeof socket_fd)];
    struct msghdr hdr = {0};
    struct cmsghdr *cmsg;

    /* Assume someone tries to start the application from command line if number of args is wrong */
    if (argc != 6) {
        fprintf(stderr,
                "This is a helper application for the aDTN service. You cannot run it directly.\n");
        return EXIT_FAILURE;
    }

    /* Parse arguments */
    remote_pid = (pid_t) strtoimax(argv[1], NULL, 0);
    abstract_path = argv[2];
    abstract_path_len = strlen(abstract_path);
    if (abstract_path_len > UNIX_PATH_MAX - 1) {
        fprintf(stderr, "Unix domain socket path is too long\n");
        return EXIT_FAILURE;
    }
    socket_domain = (int) strtol(argv[3], NULL, 0);
    socket_type = (int) strtol(argv[4], NULL, 0);
    socket_protocol = (int) strtol(argv[5], NULL, 0);

    /* Create Unix domain socket and write its file descriptor to stdout */
    uds_listener = socket(AF_UNIX, SOCK_STREAM, 0);
    if (uds_listener == -1) {
        fprintf(stderr, "Unix domain socket creation failed (%d)\n", errno);
        return EXIT_FAILURE;
    }

    /* Bind */
    memset(&bind_addr, 0, sizeof bind_addr);
    bind_addr.sun_family = AF_UNIX;
    bind_addr.sun_path[0] = 0; /* Set first byte to zero to indicate an abstract path */
    memcpy(bind_addr.sun_path + 1, abstract_path, abstract_path_len);
    if (bind(uds_listener, (struct sockaddr *) &bind_addr,
             offsetof(struct sockaddr_un, sun_path) + 1 + abstract_path_len) != 0) {
        fprintf(stderr, "bind failed (%d)\n", errno);
        close(uds_listener);
        return EXIT_FAILURE;
    }

    /* Listen */
    if (listen(uds_listener, 1) != 0) {
        fprintf(stderr, "listen failed (%d)\n", errno);
        close(uds_listener);
        return EXIT_FAILURE;
    }

		/* Inform parent process that it can connect now */
		if (write(STDOUT_FILENO, "\n", 1) != 1) {
        fprintf(stderr, "Writing to stdout failed (%d)\n", errno);
				close(uds_listener);
		}

    /* Accept */
    uds = accept(uds_listener, NULL, NULL);
    if (uds == -1) {
        fprintf(stderr, "accept failed (%d)\n", errno);
        close(uds_listener);
        return EXIT_FAILURE;
    }

    /* Close listener */
    if (close(uds_listener) != 0) {
        fprintf(stderr, "Closing Unix domain socket listener failed (%d)\n", errno);
        close(uds);
        return EXIT_FAILURE;
    }

    /* Get ID of the connected process */
    if (getsockopt(uds, SOL_SOCKET, SO_PEERCRED, &remote_cred, &remote_cred_len) != 0) {
        fprintf(stderr, "Getting SO_PEERCRED failed (%d)\n", errno);
        close(uds);
        return EXIT_FAILURE;
    }

    /* Check if the right process connected */
    if (remote_cred.pid != remote_pid) {
        fprintf(stderr, "Process with unexpected ID connected to Unix domain socket\n");
        close(uds);
        return EXIT_FAILURE;
    }

    /* Create socket */
    socket_fd = socket(socket_domain, socket_type, htons(socket_protocol));
    if (socket_fd == -1) {
        fprintf(stderr, "Socket creation failed (%d)\n", errno);
        close(uds);
        return EXIT_FAILURE;
    }

    /* Since it is not possible to send only ancillary data, a dummy zero byte is sent */
    hdr.msg_iov = &iov;
    hdr.msg_iovlen = 1;

    /* Set up ancillary data to pass the socket file descriptor */
    memset(ancillary_data, 0, sizeof ancillary_data);
    hdr.msg_control = ancillary_data;
    hdr.msg_controllen = sizeof ancillary_data;
    cmsg = CMSG_FIRSTHDR(&hdr);
    cmsg->cmsg_len = CMSG_LEN(sizeof socket_fd);
    cmsg->cmsg_level = SOL_SOCKET;
    cmsg->cmsg_type = SCM_RIGHTS;
    *((int *) CMSG_DATA(cmsg)) = socket_fd;

    /* Send it */
    if (sendmsg(uds, &hdr, 0) == -1) {
        fprintf(stderr, "sendmsg failed (%d)\n", errno);
        close(socket_fd);
        close(uds);
        return EXIT_FAILURE;
    }

    /* Wait for client to close connection */
    if (recv(uds, &dummy_message, sizeof dummy_message, 0) == -1) {
        fprintf(stderr, "recv failed (%d)\n", errno);
        close(socket_fd);
        close(uds);
        return EXIT_FAILURE;
    }

    /* Close Unix domain socket */
    if (close(uds) != 0) {
        fprintf(stderr, "Closing Unix domain socket failed (%d)\n", errno);
        return EXIT_FAILURE;
    }

    /* Close created socket */
    if (close(socket_fd) != 0) {
        fprintf(stderr, "Closing socket failed (%d)\n", errno);
        return EXIT_FAILURE;
    }

    return EXIT_SUCCESS;
}
