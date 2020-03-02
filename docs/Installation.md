# Installation Guide

## Helm Install of Driver

Prior to installing the driver, it may be necessary to configure the source for VNF Packages. See [Configuring VNF Package Location](ConfiguringVNFPackageLocation.md)

Prior to installing the driver, it may be necessary to configure the Kafka host and create mandatory topics. See [Configuring Kafka](ConfiguringKafka.md)


Download the Helm chart for the required version of the VNFM Driver. Run the following command to install the Helm chart with the default values:

```bash
helm install sol003-lifecycle-driver-<version>.tgz --name sol003-lifecycle-driver
```

## Onboarding Driver into LM

Use lmctl for onboard the driver into LM. For full details on how to install or use lmctl, refer to its documentation.

The following command will onboard the VNFM Driver into an LM environment called 'dev01':

```bash
lmctl lifecycledriver add --type sol003 --url http://sol003-lifecycle-driver:8296 dev01
```

**NOTES**:
- The above example assumes lmctl has been configured with an environment called 'dev01'. Replace this environment name accordingly
- If this configuration doesn't include the password for the environment, one will be prompted for
