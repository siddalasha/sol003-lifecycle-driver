# Configuring a Location for Retrieving VNF Packages

The VNFM driver can be configured with a location from which it can retrieve VNF packages. This can be configured via the Helm values file by setting the following property during the Helm install.

###### Example of values passed to Helm chart during install
```yaml
app:
  config:
    packageRepositoryUrl: "http://package-server/path/VnfPackage-{vnfPackageId}.zip"
```

**NOTES**:
- the URL can reference files, an HTTP target or an FTP target
- the URL must be appropriately prefixed. This includes `file:` for accessing filesystem paths, `http:` for accessing resources via the HTTP protocol, `ftp:` for accessing resources via FTP.
- the path should contain the vnfPackageId somewhere within it, as a means of identifying the appropriate package. This part of the path should be represented by the substitution variable `{vnfPackageId}`