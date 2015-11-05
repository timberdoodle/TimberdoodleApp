# Timberdoodle Android App

Repository for the source code of Timberdoodle, a device-to-device anonymous communication application for the Android platform.

## Supported hardware

aDTN, the underlying protocol, makes use of wireless broadcasts, where IEEE 802.11 operates in ad-hoc mode, requiring no access points in range of the mobile device.

Unfortunately, almost no mobile device manufacturers support ad-hoc mode adequately, hence so far we only managed to get the application to work on the *Samsung Nexus S running CyanogenMod*.

We are currently looking for workarounds to bring this application to all modern Android devices. A promising ~~solution~~ ugly hack might be connecting to several Wi-Fi Direct networks at the same time. We are not sure if we can still use broadcasts, which would be much more efficient than several unicast transmissions of the exact same packet, but we are looking into it.

## Ad-hoc mode

Ideally we would have every device with a proper implementation of the IEEE 802.11 standard, but it seems the vendors and even Google are not interested in it. This is a pity, because ad-hoc mode is ideal for situations where infrastructure is not available, for example in emergency response operations after natural disasters, or when it is being monitored/censored.

*If you are also interested in seeing ad-hoc mode on mobile devices becoming a reality, drop me a line so we can join forces.* -- meg@megfau.lt

## Credits

The initial code was developed by four students of the Technical University of Darmstadt for their final Bachelor of Science project. They are:
 - Jan Heberer
 - Christian Alexander Hühn
 - Alymbek Sadybakasov
 - Tobias-Wolfgang Otto

The [anonymous communication protocol](https://www.seemoo.tu-darmstadt.de/team/ana-barroso/adtn) that is used by Timberdoodle was designed by Ana Barroso, who also happens to be coordinating this project.
