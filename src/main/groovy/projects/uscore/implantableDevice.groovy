package projects.uscore


import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.PrecisionDate.DATE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.CATALOG_ENTRY_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.DATE_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.STRING_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represents a CXX LaborMapping for the US Core Resource Profile: US Core Implantable Device Profile.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-implantable-device.html
 *
 * The mapping works with the master data specification that is provided in xml/implantableDevice.xml
 * The xml file can be imported over CXX xml import interface
 * The corresponding code systems are provided rudimentary and are to be completed.
 *
 *
 * @author Mike Wähnert, Jonas Küttner
 * @since v.1.14.0, CXX.v.2022.1.0
 */

device {

  if ("US_CORE_IMPLANTABLE_DEVICE" != context.source[laborMapping().laborFinding().laborMethod().code()]) {
    return
  }

  id = "Device/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-implantable-device")
  }

  final def lblvDeviceId = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "DEVICE_IDENTIFIER" }

  final def lblvCarrierHRF = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "DEVICE_UDI_IDENTIFIER" }

  udiCarrier {
    if (lblvDeviceId) {
      deviceIdentifier = lblvDeviceId[STRING_VALUE]
    }
    if (lblvCarrierHRF) {
      carrierHRF = lblvCarrierHRF[STRING_VALUE]
    }
  }

  final def lblvManufacturDate = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "DEVICE_MANUFACTURE_DATE" }

  if (lblvManufacturDate) {
    manufactureDate {
      date = lblvManufacturDate[DATE_VALUE][DATE]
    }
  }

  final def lblvExpirationDate = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "DEVICE_EXPIRATION_DATE" }

  if (lblvExpirationDate) {
    expirationDate {
      date = lblvExpirationDate[DATE_VALUE][DATE]
    }
  }

  final def lblvLotNumber = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "DEVICE_LOT_NUMBER" }

  if (lblvLotNumber) {
    lotNumber = lblvLotNumber[STRING_VALUE]
  }

  final def lblvSerialNumber = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "DEVICE_SERIAL_NUMBER" }

  if (lblvSerialNumber) {
    serialNumber = lblvSerialNumber[STRING_VALUE]
  }

  final def lblvType = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "DEVICE_TYPE" }

  if (lblvType) {
    type {
      coding {
        system = "http://snomed.info/sct"
        code = (lblvType[CATALOG_ENTRY_VALUE] as List)?.get(0)?.getAt(CODE)
      }
    }
  }


  patient {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

}


