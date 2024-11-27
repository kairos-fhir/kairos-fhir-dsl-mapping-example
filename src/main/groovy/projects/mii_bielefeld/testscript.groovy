package projects.mii_bielefeld

import com.fasterxml.jackson.databind.ObjectMapper

condition {
  new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(context.source)
}

