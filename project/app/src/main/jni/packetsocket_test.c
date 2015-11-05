/* This is a test application for the Timberdoodle packet socket. It will receive raw Ethernet
 * packets from the interface and with the EtherType specified in the arguments. Then it will 
 * increase the value of the payload bytes by one and send the packet. */

#include <arpa/inet.h>
#include <errno.h>
#include <limits.h>
#include <linux/if_packet.h>
#include <net/ethernet.h>
#include <net/if.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/ioctl.h>
#include <unistd.h>

void show_usage(const char *program_name) {
    fprintf(stderr, "Usage: %s <network_interface> <EtherType>\n", program_name);
}

/*
 * Tries to parse "str" to an unsigned 16 bit integer. On success 0 is returned and the parsed
 * integer is stored in "value". On error -1 is returned.
 */
static int parse_uint16(const char *str, uint16_t *value) {
    char *endptr;
    unsigned long parsed_ulong;

    /* Try to parse it */
    parsed_ulong = strtoul(str, &endptr, 0);
    if ((parsed_ulong == ULONG_MAX && errno == ERANGE) || *endptr != '\0' ||
        parsed_ulong > UINT16_MAX) {
        return -1;
    }

    /* Set result and report success */
    *value = (uint16_t) parsed_ulong;
    return 0;
}

int main(int argc, char **argv) {
    const char *iface_name;
    uint16_t etherType;
    struct ifreq ifr;
    struct sockaddr_ll bind_addr;
    char packet[ETHER_MAX_LEN];
    struct ether_header *hdr = (struct ether_header *) packet;

    /* Check number of arguments */
    if (argc != 3) {
        show_usage(argv[0]);
        return EXIT_FAILURE;
    }

    /* Get network interface argument */
    if (strlen(argv[1]) > IFNAMSIZ) {
        fprintf(stderr, "Network interface name is too long\n");
    }
    iface_name = argv[1];

    /* Parse EtherType argument */
    if (parse_uint16(argv[2], &etherType) != 0) {
        fprintf(stderr, "Invalid EtherType specified\n");
        return EXIT_FAILURE;
    }

    /* Create packet socket */
    int fd = socket(AF_PACKET, SOCK_RAW, htons(etherType));
    if (fd == -1) {
        fprintf(stderr, "Could not create socket (%d)\n", errno);
        return EXIT_FAILURE;
    }

    /* Get network interface index */
    strcpy(ifr.ifr_name, iface_name);
    if (ioctl(fd, SIOCGIFINDEX, &ifr) == -1) {
        fprintf(stderr, "ioctl for SIOCGIFINDEX failed (%d)\n", errno);
        close(fd);
        return EXIT_FAILURE;
    }

    /* Bind socket */
    memset(&bind_addr, 0, sizeof bind_addr);
    bind_addr.sll_family = AF_PACKET;
    bind_addr.sll_protocol = htons(etherType);
    bind_addr.sll_ifindex = ifr.ifr_ifindex;
    if (bind(fd, (const struct sockaddr *) &bind_addr, sizeof bind_addr) != 0) {
        fprintf(stderr, "bind failed (%d)\n", errno);
        close(fd);
        return EXIT_FAILURE;
    }

    /* Receive and send modified packet */
    printf("Waiting for packets...\n");
    while (1) {
        ssize_t num_received, num_sent;
        size_t i;

        /* Receive packet */
        num_received = recv(fd, packet, sizeof packet, MSG_TRUNC);
        if (num_received == -1) {
            fprintf(stderr, "recv failed (%d)\n", errno);
            close(fd);
            return EXIT_FAILURE;
        }

        /* Check packet size */
        if (num_received > sizeof packet) {
            printf("Ignoring too large packet (%zd bytes)\n", num_received);
            continue;
        } else if (num_received < ETHER_HDR_LEN) {
            printf("Ignoring too small packet (%zd bytes)\n", num_received);
            continue;
        }

        /* Show info for received packet */
        printf("recv: d=%02X:%02X:%02X:%02X:%02X:%02X s=%02X:%02X:%02X:%02X:%02X:%02X t=0x%04X l=%zd\n",
               hdr->ether_dhost[0], hdr->ether_dhost[1], hdr->ether_dhost[2], hdr->ether_dhost[3],
               hdr->ether_dhost[4], hdr->ether_dhost[5], hdr->ether_shost[0], hdr->ether_shost[1],
               hdr->ether_shost[2], hdr->ether_shost[3], hdr->ether_shost[4], hdr->ether_shost[5],
               htons(hdr->ether_type), num_received);

        /* Increment value of payload bytes */
        for (i = ETHER_HDR_LEN; i < num_received; ++i) {
            ++packet[i];
        }

        /* Send packet */
        num_sent = send(fd, packet, (size_t) num_received, 0);
        if (num_sent == -1) {
            fprintf(stderr, "send failed (%d)\n", errno);
            close(fd);
            return EXIT_FAILURE;
        }
    }
}
