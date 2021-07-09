package projects.gecco.crf

import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.PrecisionDate

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

immunization {

  // just a showcase to retrieve cxx data from a crf
  def crfItems = context.source[studyVisitItem().crf().items()]
  List<String> crfItemVaccineDates = new ArrayList<>()
  crfItems.each { def crfItem ->
    crfItemVaccineDates.add(crfItem[CrfItem.DATE_VALUE][PrecisionDate.DATE] as String)
  }

  // result should look something like this
  // def crfItemVaccineDates = ["2021-07-03", "2021-07-01", "2021-07-07", "2021-07-02"]

  // Date of last vaccination --> "Date of full immunization
  occurrenceDateTime {
    date = selectMostRecentDate(crfItemVaccineDates)
  }
}

static String selectMostRecentDate(final List<String> vdl) {
  return vdl ? (vdl.sort().last()) : null
}

