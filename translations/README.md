# translations

`translations` is a module that provides a simple interface called
`Translations` with the single implementation `RemoteJsonFileTranslations` which
contains localized strings that are fetched from a file on S3.

This file is updated periodically by a python cron job that runs in the Kubernetes cluster.
