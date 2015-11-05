/* This is a JNI library for receiving a packet socket file descriptor via a Unix domain socket and
 * using it in Java. */

#include <jni.h>

#include <arpa/inet.h>
#include <errno.h>
#include <linux/if_packet.h>
#include <linux/un.h>
#include <net/if.h>
#include <sys/socket.h>
#include <unistd.h>

// Creates a PacketSocketException from the specified error message and error number.
static jobject createException(JNIEnv *env, const char *message, int err_no) {
    jclass cls = (*env)->FindClass(env, "de/tu_darmstadt/adtn/packetsocket/PacketSocketException");
    jmethodID ctor = (*env)->GetMethodID(env, cls, "<init>", "(Ljava/lang/String;I)V");
    return (*env)->NewObject(env, cls, ctor, (*env)->NewStringUTF(env, message), (jint) err_no);
}

static int read_fd_from_uds(JNIEnv *env, const char *abstract_path) {
    /* Unix domain socket */
    size_t abstract_path_len;
    int uds;
    struct sockaddr_un server_addr;

    /* Holds the received file descriptor */
    const size_t size_of_fd = sizeof(int);

    /* File descriptor passing */
    uint8_t dummy_message;
    struct iovec iov = {.iov_base = &dummy_message, .iov_len = sizeof dummy_message};
    unsigned char ancillary_data[CMSG_SPACE(size_of_fd)];
    struct msghdr hdr = {0};
    struct cmsghdr *cmsg;

    /* Check if the length of the specified abstract Unix socket path is withing range */
    abstract_path_len = strlen(abstract_path);
    if (abstract_path_len > UNIX_PATH_MAX - 1) {
        (*env)->Throw(env, createException(env, "Unix path name is too long", 0));
        return -1;
    }

    /* Create Unix domain socket */
    uds = socket(AF_UNIX, SOCK_STREAM, 0);
    if (uds == -1) {
        (*env)->Throw(env, createException(env, "Could not create Unix socket", errno));
        return -1;
    }

    /* Connect to server in abstract namespace */
    memset(&server_addr, 0, sizeof server_addr);
    server_addr.sun_family = AF_UNIX;
    server_addr.sun_path[0] = 0; /* Set first byte to zero to indicate an abstract path */
    memcpy(server_addr.sun_path + 1, abstract_path, abstract_path_len);
    if (connect(uds, (struct sockaddr *) &server_addr,
                offsetof(struct sockaddr_un, sun_path) + 1 + abstract_path_len) != 0) {
        int err_no = errno;
        close(uds);
        (*env)->Throw(env, createException(env, "connect failed", err_no));
        return -1;
    }

    /* Since it is not possible to send only ancillary data, a dummy byte is received */
    hdr.msg_iov = &iov;
    hdr.msg_iovlen = 1;

    /* Set up buffer to ancillary data receive buffer */
    hdr.msg_control = ancillary_data;
    hdr.msg_controllen = sizeof ancillary_data;

    /* Receive message */
    if (recvmsg(uds, &hdr, 0) == -1) {
        int err_no = errno;
        close(uds);
        (*env)->Throw(env, createException(env, "recvmsg failed", err_no));
        return -1;
    }

    /* Close Unix domain socket */
    if (close(uds) != 0) {
        (*env)->Throw(env, createException(env, "Could not close Unix socket", errno));
        return -1;
    }

    /* Check if received message is valid */
    cmsg = CMSG_FIRSTHDR(&hdr);
    if (dummy_message != 0 || hdr.msg_flags & MSG_CTRUNC ||
        hdr.msg_controllen != sizeof ancillary_data || cmsg->cmsg_len != CMSG_LEN(size_of_fd) ||
        cmsg->cmsg_level != SOL_SOCKET || cmsg->cmsg_type != SCM_RIGHTS) {
        (*env)->Throw(env, createException(env, "Received invalid ancillary message", 0));
        return -1;
    }

    /* Return the received file descriptor */
    return *(int *) CMSG_DATA(cmsg);
}

JNIEXPORT jint JNICALL
Java_de_tu_1darmstadt_adtn_packetsocket_PacketSocket_ioctlGetInterfaceIndex(JNIEnv *env,
                                                                            jobject obj,
                                                                            jint fileDescriptor,
                                                                            jstring interfaceName) {
    const char *iface_name_chars;
    struct ifreq ifr;

    /* Set up ifreq structure: Copy interface name to ifr_name */
    iface_name_chars = (*env)->GetStringUTFChars(env, interfaceName, NULL);
    strlcpy(ifr.ifr_name, iface_name_chars, IFNAMSIZ);
    (*env)->ReleaseStringUTFChars(env, interfaceName, iface_name_chars);

    /* Call ioctl with SIOCGIFINDEX to get the interface index from its name */
    if (ioctl(fileDescriptor, SIOCGIFINDEX, &ifr) == -1) {
        (*env)->Throw(env, createException(env, "ioctl for SIOCGIFINDEX failed", errno));
        return -1;
    }

    return ifr.ifr_ifindex;
}

JNIEXPORT jint JNICALL
Java_de_tu_1darmstadt_adtn_packetsocket_PacketSocket_setsockoptInt(JNIEnv *env,
                                                                   jobject obj,
                                                                   jint sockfd, jint level,
                                                                   jint optname, jint value) {
    return setsockopt(sockfd, level, optname, &value, sizeof value);
}

JNIEXPORT void JNICALL
Java_de_tu_1darmstadt_adtn_packetsocket_PacketSocket_bind(JNIEnv *env, jobject obj,
                                                          jint sockfd, jint protocol,
                                                          jint ifindex) {
    /* Set up sockaddr_ll structure */
    struct sockaddr_ll addr;
    memset(&addr, 0, sizeof addr);
    addr.sll_family = AF_PACKET;
    addr.sll_protocol = htons(protocol);
    addr.sll_ifindex = ifindex;

    if (bind(sockfd, (const struct sockaddr *) &addr, sizeof addr) == -1) {
        (*env)->Throw(env, createException(env, "bind failed", errno));
    }
}

JNIEXPORT void JNICALL
Java_de_tu_1darmstadt_adtn_packetsocket_PacketSocket_close(
        JNIEnv *env, jobject obj,
        jint fileDescriptor) {
    if (close(fileDescriptor) == -1) {
        (*env)->Throw(env, createException(env, "close failed", errno));
    }
}

JNIEXPORT jint JNICALL
Java_de_tu_1darmstadt_adtn_packetsocket_PacketSocket_recv(JNIEnv *env, jobject obj,
                                                          jint fileDescriptor,
                                                          jobject buffer,
                                                          jint offset, jint count,
                                                          jint flags) {
    /* Apply offset to buffer pointer */
    void *buf = (*env)->GetDirectBufferAddress(env, buffer) + offset;

    /* Call recv */
    ssize_t result = recv(fileDescriptor, buf, (size_t) count, flags);
    if (result == -1) (*env)->Throw(env, createException(env, "recv failed", errno));
    return (jint) result;
}

JNIEXPORT jint JNICALL
Java_de_tu_1darmstadt_adtn_packetsocket_PacketSocket_send(JNIEnv *env, jobject obj,
                                                          jint fileDescriptor,
                                                          jobject buffer,
                                                          jint offset, jint count,
                                                          jint flags) {
    /* Apply offset to buffer pointer */
    const void *buf = (*env)->GetDirectBufferAddress(env, buffer) + offset;

    /* Call send */
    ssize_t result = send(fileDescriptor, buf, (size_t) count, flags);
    if (result == -1) (*env)->Throw(env, createException(env, "send failed", errno));
    return (jint) result;
}

JNIEXPORT jint JNICALL
Java_de_tu_1darmstadt_adtn_packetsocket_PacketSocket_readFileDescriptorFromUds(JNIEnv *env,
                                                                               jobject obj,
                                                                               jstring abstractPath) {
    /* Call read_fd_from_uds with "const char *" of abstractPath */
    const char *abstract_path_chars = (*env)->GetStringUTFChars(env, abstractPath, NULL);
    int fd = read_fd_from_uds(env, abstract_path_chars);
    (*env)->ReleaseStringUTFChars(env, abstractPath, abstract_path_chars);
    return fd;
}
