# Timberdoodle Android App

Timberdoodle is a device-to-device anonymous communication application for the Android platform. More information on the anonymity features of the underlying protocol, aDTN, can be found [here](https://www.seemoo.tu-darmstadt.de/team/ana-barroso/adtn).

Devices exchange messages directly between them, with no intermediate infrastructure required. In this implementation of aDTN, we have to modify the protocol to work over unicast links, as we are using Bluetooth on the link layer. The original specification of aDTN assumes broadcasts are available. We take care that this change does not compromise the users' anonymity.

Ideally we would have every device with a proper implementation of the IEEE 802.11 standard, which would allow us to use broadcasting via ad-hoc mode, instead of unicasting with Bluetooth, but it seems the vendors and even Google are not interested in it. This is a pity, because ad-hoc mode would be much more efficient and ideal for situations where infrastructure is not available, for example in emergency response operations after natural disasters, or when it is being monitored/censored.

*If you are also interested in seeing ad-hoc mode on mobile devices becoming a reality, drop me a line so we can join forces.* -- meg@megfau.lt

## Credits

The initial code was developed by four students of the Technical University of Darmstadt for their final Bachelor of Science project. They are:
 - Jan Heberer
 - Christian Alexander Hühn
 - Alymbek Sadybakasov
 - Tobias-Wolfgang Otto

aDTN, the [anonymous communication protocol](https://www.seemoo.tu-darmstadt.de/team/ana-barroso/adtn) that is used by Timberdoodle was designed by Ana Barroso, who is also coordinating this project.
