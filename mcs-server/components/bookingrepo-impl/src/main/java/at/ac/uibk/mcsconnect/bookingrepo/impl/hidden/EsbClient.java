package at.ac.uibk.mcsconnect.bookingrepo.impl.hidden;

import at.ac.uibk.mcsconnect.functional.osgi.OsgiPropertyReader;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriBuilder;

import java.net.URI;
import java.util.Map;

@Component(
        name = "at.ac.uibk.mcsconnect.bookingrepo.impl.hidden.EsbClient",
        immediate = true
)
public class EsbClient implements EsbClientCalls {

    private static final Logger LOGGER = LoggerFactory.getLogger(EsbClient.class);

    // config keys
    private static final String CFG_ESB_USERNAME = "esb.username";
    private static final String CFG_ESB_PASSWORD = "esb.password";
    private static final String CFG_ESB_URL = "esb.url";

    // config
    private String esbUsername;
    private String esbPassword;
    private URI esbUri;

    //private final URI esbBaseTarget = URI.create("https://texassa2.uibk.ac.at/cxf/vle-connect/sis"); // base path
    //private Secrets secrets;

    @Activate
    public EsbClient(final Map<String,?> properties) {
        handleProperties(properties);
    }

    @Modified
    public synchronized void updated(final Map<String,?> properties) {
       handleProperties(properties);
    }

    private void handleProperties(Map<String,?> properties) {
        OsgiPropertyReader reader = OsgiPropertyReader.create(properties);
        this.esbUsername = reader.getAsString(CFG_ESB_USERNAME).getOrElse(EsbClientDefaults.DEFAULT_ESB_USERNAME);
        this.esbPassword = reader.getAsString(CFG_ESB_PASSWORD).getOrElse(EsbClientDefaults.DEFAULT_ESB_PASSWORD);
        this.esbUri = reader.getAsURI(CFG_ESB_URL).getOrElse(EsbClientDefaults.DEFAULT_ESB_URL);
    }

    @Override
    public TvrBookings fetchBookingsForUserId(String userId, String roles, String bookingsMinDate, String bookingsMaxDate) throws WebApplicationException
    {
        LOGGER.info(String.format("%s.fetchBookingsForUserId(userId: %s, roles: %s, bookingsMinDate: %s, bookingsMaxDate: %s)", this, userId, roles, bookingsMinDate, bookingsMaxDate));

        URI bookingsTarget = UriBuilder
                .fromUri(this.esbUri) // init base URI from URI
                .build();

            LOGGER.debug("ESB URI: {}", bookingsTarget.toString());
            EsbClientCalls esb = JAXRSClientFactory.create(
                    bookingsTarget.toString(),
                    EsbClientCalls.class,
                    this.esbUsername,
                    this.esbPassword,
                    null);
            TvrBookings tvrBookings = esb.fetchBookingsForUserId(userId, roles, bookingsMinDate, bookingsMaxDate);
            LOGGER.info(String.format("%s.fetchBookingsForUserId(userId: %s, roles: %s, bookingsMinDate: %s, bookingsMaxDate: %s) returning: %s", this, userId, roles, bookingsMinDate, bookingsMaxDate, tvrBookings));
            return tvrBookings;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}