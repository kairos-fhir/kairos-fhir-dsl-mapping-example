# Examples

## The message

1. The source message that will be transformed with the groovy script. The message contains just dummy data put together according to the ISIK FHIR 
profiles. 
2. The transformed bundle, i.e. the output of the groovy transformation.

## Transformation logic

The incoming message contains one Patient and one Condition resource. These ISIK resources are 
transformed into 3 HDRP resources, one Patient, one Condition and one Observation.

To successfully link these resources in HDRP, we need to use correct references. Since it is expected that
records can be sent to HDRP multiple times, they need to be created if they do not exist or updated by a business
identifier if they exist already. 

The HDRP Bundle import deviates from the FHIR specs. We support POST, PUT, and DELETE operation. POST and PUT both follow a 
create if not exists or update logic. For POST, HDRP uses a business key to resolve the record in its database. For a patient
this would be the Patient.identifier. In some cases, the business keys consists of different resource elements that
can also include fields outside the identifier element. We are working on profiling these natural identifier fields in 
separate StructureDefinitions on Simplifier.net.

When using PUT, HDRP will resolve the records by the logical FHIR ID given in the resource.




