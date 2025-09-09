<img src="images/Logo.png" width="250" alt="IQVIA Logo"/>

# FHIR Bulk Data Export

Bulk data export is used to export configured export customexport asynchronously to the file system or to an FHIR endpoint.
The configurational setup and the FHIR operations used to control FHIR bulk data export are described in this section.

## Configuration

To enable the bulk data export, set the following properties in the `ProjectConfig.json`

```json
{
  "bulkExportEnable": {
    "value": true
  }
}
```

## Bulk Data Export FHIR REST operations

The export is controlled by multiple FHIR REST operations.
All REST-calls need to be authenticated using BasicAuth or a BearerToken. Depending on the User rights, clear text access to the
patient records is possible.

### Kick-Off Query (`$export`)

Creates an export job and submits it for execution.

```GET <hdrp-base>/fhir/r4/$export```

> [!IMPORTANT]
> The HTTP header `Prefer: respond-async` must be supplied.

| **Parameter**   | **Type** | **Description**                                                                                                                                                                                                                                                                                                                                      | **Default**       |
|-----------------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------|
| `_outputFormat` | String   | Specifies the output format for the export. The arguments are mapped as follows: `"application/fhir+json"`, `"application/json"` -> JSON, `"application/fhir+xml"`, `"application/xml"` -> XML, `"application/fhir+ndjson"`, `"application/x-ndjson"`, `"application/ndjson"` -> NDJSON. If the parameter is not specified, JSON is used by default. | JSON              |
| `_type`         | String   | Comma-separated list of FHIR `ResourceType`s. The configured `ExportResourceMappings` are filtered based on the specified list.                                                                                                                                                                                                                      | - (exporting all) |
| `_since`        | Date     | If specified, only patients modified after this date are included in the export. Requires [Patient modification and delete modification](#incremental-bulk-exports) logging to be enabled.                                                                                                                                                           | - (exporting all) |
| `projectFilter` | String   | Comma-separated list of export customexport to include in the export.                                                                                                                                                                                                                                                                                    | - (exporting all) |

The request will return a 202 status upon successful submission. The response will contain the `Content-Location` header which will contain
a URL that can be used to query the job status.

### Poll Export Status Query (`$export-poll-status`)

After submission, the job status can be queried using the _jobId assigned by HDRP. The URL returned in the `Content-Location` header in the response
of the kick-off query
```GET <hdrp-base>/fhir/r4/$export-poll-status```

| **Parameter** | **Type** | **Description**             | **Default** |
|---------------|----------|-----------------------------|-------------|
| `_jobId`      | String   | the job id assigned by HDRP | -           |

Depending on the status, different HTTP status codes will be returned

| **Status Code** | **Description**                                 |
|-----------------|-------------------------------------------------|
| 202             | The job was submitted or is currently executed. |
| 200             | The job is finished successfully.               |
| 410             | The job was cancelled by a client               |
| 500             | The export failed on an unexpected error        |

After successful completion of an export job, the Response will contain a list to the result files

```json
{
  "transactionTime": "2025-01-24T17:19:39.153+01:00",
  "output": [
    {
      "type": "Patient",
      "url": "http://<hdrp-base>/fhir/r4/$export-result-file?_jobId=381e381d-3817-4c87-9ee2-238557c586c2&_fileId=d5d95624-666b-4eb8-b78c-5747565e89a1"
    },
    {
      "type": "Condition",
      "url": "http://<hdrp-base>/fhir/r4/$export-result-file?_jobId=381e381d-3817-4c87-9ee2-238557c586c2&_fileId=7c9d560f-5f86-436e-b737-f14d7d06a2e7"
    },
    {
      "type": "Observation",
      "url": "http://<hdrp-base>/fhir/r4/$export-result-file?_jobId=381e381d-3817-4c87-9ee2-238557c586c2&_fileId=bb8dd976-4f86-4248-a42b-77bd303cd0a7"
    },
    {
      "type": "Specimen",
      "url": "http://<hdrp-base>/fhir/r4/$export-result-file?_jobId=381e381d-3817-4c87-9ee2-238557c586c2&_fileId=b5cb85ab-4ac9-4dfd-a1d0-7a6604918f76"
    }
  ],
  "deleted": [
    {
      "url": "http://<hdrp-base>/fhir/r4/$export-result-file?_jobId=381e381d-3817-4c87-9ee2-238557c586c2&_fileId=84a48451-8ea2-4e79-af91-d9cf71b1cfec"
    }
  ]
}
```

The given urls can be used to access the exported resources via the `$export-result-file` operation.

### Export Result Files Query (`$export-result-file`)

The exported files can be queried using the provided urls in the response of the `$export-result-file` after completion of the export job
The exported FHIR Bundle will be returned in the Body of the HTTP response.

```GET <hdrp-base>/fhir/r4/$export-result-file```

| **Parameter** | **Type** | **Description**                                            | **Default** |
|---------------|----------|------------------------------------------------------------|-------------|
| `_jobId`      | String   | the job id assigned by HDRP                                | -           |
| `_fileId`     | String   | the fileId (provided by the `$export-result-file` response | -           |

Depending on the status different HTTP status codes will be returned

| **Status Code** | **Description**                                                            |
|-----------------|----------------------------------------------------------------------------|
| 200             | The file was resolved and the Bundle returned successfully in the Response |
| 500             | The file was not found or an unexpected error occurred.                    |

### Cancellation Query (`$cancel-export`)

A running export can be canceled using this operation. The jobId must be supplied as the `_jobId` query parameter. The running export will be notified
to terminate after the currently processed pages have been exported.

| **Parameter** | **Type** | **Description**             | **Default** |
|---------------|----------|-----------------------------|-------------|
| `_jobId`      | String   | the job id assigned by HDRP | -           |

If successful, the operation will return HTTP status code 202.

### Resubmission Query (`$resume-export`)

If an export was terminated by a client using `$cancel-export` the cancel export operation or failed on an unexpected error,
the export can be resumed from where it stopped. If used, the export will restart processing at the last processed resource mapping.
The jobId of the export job has to be supplied in the `_jobId` query parameter.

| **Parameter** | **Type** | **Description**             | **Default** |
|---------------|----------|-----------------------------|-------------|
| `_jobId`      | String   | the job id assigned by HDRP | -           |

If the operation was successful, HTTP status code 202 will be returned.

## Cleanup

The bulk data export jobs to the file system are scheduled for cleanup after a configurable time period after termination of the job.
The retention time can be configured in the ```centraxx-dev.properties``` file with this property:

```
interfaces.fhir.custom.bulkexport.resultcache.expire=24
```

It specifies the time in hours after which the exported files are removed from the file system and job metadata is removed from the database.

## Incremental Bulk Exports

The Bulk export can be used with the `_sinceDate` parameter. Only data for which the
patient records were changed after the provided date will be exported. This feature requires the patient modification and delete modification logging
to be enabled in the `centraxx-dev.properties`

```
interfaces.fhir.custom.bulkexport.patientModificationLogging.enable=true
interfaces.fhir.custom.bulkexport.deleteModificationLogging.enable=true
```
