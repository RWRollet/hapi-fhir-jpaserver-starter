package ca.uhn.fhir.jpa.starter;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * We expect an exception when fetching the none existing third page with link
 * http://localhost:8080/fhir?_getpages=...&_getpagesoffset=6&_count=3
 *
 * @author Rene Wiegmann Rollet (rene.wiegmann.rollet@fokus.fraunhofer.de)
 */
class TriggerIndexException {

  private static final String SERVER_BASE = "http://localhost:8080/fhir/";

  public static void main(String[] args) {
    IGenericClient client = FhirContext.forR4().newRestfulGenericClient(SERVER_BASE);

    addTestResourcesToServerIfNoneExist(client);

    System.out.println("Search for ressources and get first page.");
    Bundle resultInitial = searchForResources(client);

    while (resultInitial.getLink(IBaseBundle.LINK_NEXT) != null) {
      System.out.println("Get next page: " + resultInitial.getLink(IBaseBundle.LINK_NEXT).getUrl());
      // The second call of this method should throw an exception
      resultInitial = client.loadPage().next(resultInitial).execute();
    }
    System.out.println("No exception thrown... this is unexpected.");
  }

  private static void addTestResourcesToServerIfNoneExist(IGenericClient client) {
    if (!searchForResources(client).hasEntry()) {
      System.out.println("Add four resources to the server.");
      for (int i = 0; i < 4; i++) {
        client.create().resource(new Binary()).execute();
      }
    }
  }

  private static Bundle searchForResources(IGenericClient client) {
    return client.search().forResource(Binary.class).returnBundle(Bundle.class).execute();
  }
}
