package at.ac.uibk.mcsconnect.http.impl.hidden;

// DISABLED

// THIS CAN BE USED INSTEAD OF RSA IF NECESSARY FOR MORE CONTROL

import at.ac.uibk.mcsconnect.bookingrepo.api.BookingRepo;
import at.ac.uibk.mcsconnect.common.api.McsConfiguration;
import at.ac.uibk.mcsconnect.http.api.PublicResourceApi;
import at.ac.uibk.mcsconnect.http.impl.PublicResourceImpl;
import at.ac.uibk.mcsconnect.person.api.UserFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstanceFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.RoomRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;

@Component(// run in karaf: scr:list to see whether org.apache.cxf.dosgi.dsw.handlers.rest.RsProvider is active
    immediate = true, //run in karaf: rsa:endpoints to see whether this component was detected
        //service = {PublicResourceRoot.class},
    name = "at.ac.uibk.mcsconnect.http.impl.ManualRestServiceImpl", //
    property = //
        {
        } // see https://issues.apache.org/jira/browse/DOSGI-140
)
@Path("/public")
public class ManualJaxRsHttpServerImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManualJaxRsHttpServerImpl.class);

    private Server server;
    //private PublicResourceImpl publicResourceImp = new PublicResourceImpl();

    @Reference
    private RoomRepo roomRepo;
    @Reference
    private BookingRepo bookingRepo;
    @Reference
    private UserFactory userFactory;
    @Reference
    private RecordingInstanceFactory recordingInstanceFactory;
    @Reference
    private McsConfiguration mcsConfiguration;

    @Activate
    public void activate() throws Exception {
        LOGGER.info(String.format("%s.activate() called.", this.getClass().getSimpleName()));
        // JAXRSServerFactoryBean jaxrsServerFactory = RuntimeDelegate.getInstance().createEndpoint(new HelloWorldApp(), JAXRSServerFactoryBean.class);
        // JAXRSServerFactoryBean creates a Server inside CXF which starts listening for requests on the URL specified.
        JAXRSServerFactoryBean endpoint = new JAXRSServerFactoryBean();
        // RequestScopeResourceFactory
        endpoint.setBus(BusFactory.getDefaultBus());
        // setResourceClasses() is for root resources only, use setProvider() or setProviders() for @Provider-annotated classes.
        endpoint.setResourceClasses(PublicResourceApi.class);
        // By default, the JAX-RS runtime is responsible for the lifecycle of resource classes, default lifecycle is per-request. You can set the lifecycle to singleton by using following line:

        endpoint.setResourceProvider(PublicResourceApi.class, new SingletonResourceProvider(new PublicResourceImpl(roomRepo, bookingRepo, userFactory, recordingInstanceFactory, mcsConfiguration)));//new PerRequestResourceProvider(PublicResourceImpl.class)); // @PostConstruct
        endpoint.setAddress("/mcs-connect"); // /cxf/whateverissethere/resourcepath //TODO make configurable
        //endpoint.setProvider(new EntityResolverFilter()); // Logic pushed into ResourceMapper and Resource
        //endpoint.setProvider(new ContainerRequestContextImpl());
        ObjectMapper mapper = new ObjectMapper();
        //mapper.registerModule(new JsonLocalDateTimeSerializerModule());
        //mapper.registerModule(new JsonLocalDateTimeDeserializerModule());
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);//switched from true after change from String to LocalDateTime
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        //mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        endpoint.setProvider(new JacksonJsonProvider(mapper));
        //bean.getFeatures().add(swagger);
        LOGGER.info(String.format("Starting endpoint %s with features: %s", endpoint, endpoint.getFeatures()));
        server = endpoint.create();
    }

    @Deactivate
    public void deactivate() throws Exception {
        if (server != null) {
            try {
                LOGGER.info(String.format("Deactivating server %s", this));
                server.stop();
                server.destroy();
            } catch (Exception e) {
                LOGGER.warn(String.format("Cannot kill server %s", this));
            }
        }
    }

    //private Supplier<OpenApiFeature> bootstrapOpenApi() {
    //    openApi -> {
    //        openApi.
    //bean.setFeatures(Arrays.asList(new Swagger2Feature()));
    //Swagger2Feature swagger = new Swagger2Feature();
    //swagger.setBasePath("/");
    //swagger.setScanAllResources(false);
    //swagger.setPrettyPrint(true);
    //swagger.setSupportSwaggerUi(true);
    //swagger.setTitle("Camel Catalog and Connector Catalog REST Api");
    //swagger.setDescription("REST Api for the Camel Catalog and Connector Catalog");
    //swagger.setVersion(catalog.getCatalogVersion());
    //swagger.setContact("Apache Camel");
    //    }
    //}

}