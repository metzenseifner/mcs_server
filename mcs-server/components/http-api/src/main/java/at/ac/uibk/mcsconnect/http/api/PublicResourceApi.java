package at.ac.uibk.mcsconnect.http.api;

import at.ac.uibk.mcsconnect.http.api.hidden.datatransferobjects.RecordingInstanceIntermediateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.cxf.jaxrs.ext.MessageContext;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * JAX-RS Resource Class
 */
public interface PublicResourceApi {

    // For documentation,
    // see https://github.com/apache/cxf/tree/master/distribution/src/main/release/samples/jax_rs/description_openapi_v3/src/main/java/demo/jaxrs/openapi/server

    //@JsonView(Views.Public.class)
    @OldApiEndpoint(url = "/cxf/mcs-connect/terminals/whoami")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/terminals/whoami")
    @Operation(summary = "Get necessary information for other API calls.",
            description = "An entity resolver checks your ip address with its registry. " +
                    "Assuming a match is found, that information is provided." +
                    "As a convenience and to reduce the number of calls necessary" +
                    "The terminal's greater context is also provided.")
    @ApiResponse(responseCode = "200",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    //schema = @Schema(implementation = UibkTerminal.class),
                    examples = {
                            @ExampleObject(
                                    name = "terminal",
                                    summary = "UibkTerminal call ",
                                    value = "{" +
                                            "  \"terminalId\": \"t_nm-pc11\"," +
                                            "  \"networkTarget\": {" +
                                            "    \"host\": \"138.232.17.223\"," +
                                            "    \"port\": 443" +
                                            "  }," +
                                            "  \"room\": {" +
                                            "    \"id\": \"avstudio\"," +
                                            "    \"name\": \"AV Studio\"," +
                                            "    \"user\": {" +
                                            "      \"userId\": \"c102273\"," +
                                            "      \"email\": \"Jonathan.Komar@uibk.ac.at\"," +
                                            "      \"displayName\": \"Jonathan Lee Komar\"," +
                                            "      \"bookings\": [" +
                                            "        {" +
                                            "          \"bookingId\": 3601840," +
                                            "          \"timeBegin\": \"2020-04-09T09:00:00\"," +
                                            "          \"timeEnd\": \"2020-04-09T22:00:00\"," +
                                            "          \"resourceId\": 1334," +
                                            "          \"resourceName\": \"dVs\"," +
                                            "          \"locationName\": \"Innrain\"," +
                                            "          \"courseId\": 293584," +
                                            "          \"groupId\": 474764," +
                                            "          \"termId\": \"2020S\"," +
                                            "          \"courseNumber\": 171001," +
                                            "          \"courseName\": \"DMLT Aufzeichnungen\"," +
                                            "          \"groupIndex\": 0" +
                                            "        }" +
                                            "      ]" +
                                            "    }," +
                                            "    \"recorders\": [" +
                                            "      {" +
                                            "        \"name\": \"Main\"," +
                                            "        \"id\": \"r_avstudio_01\"," +
                                            "        \"firmwareVersion\": \"?\"," +
                                            "        \"recorderRunningState\": \"STOPPED\"" +
                                            "      }" +
                                            "    ]," +
                                            "    \"actualRecordingRunningState\": \"STOPPED\"," +
                                            "    \"recordingInstance\": null" +
                                            "  }" +
                                            "}"
                            )
                    }
            )
    )
    Response whoami(@Context MessageContext messageContext);

    @OldApiEndpoint(
            url = "/cxf/mcs-connect/rooms/${this.roomId}/recording/polledrecordingrunningstate",
            description = "Thinking was to provide a way for the client to detect errors by polling every N seconds."
    )
    @GET
    @Path("/rooms/{roomId}/recording/polledrecordingrunningstate")
    @Operation(summary = "Callback to allow client to check the running state of the recording.")
    Response getPolledRecordingRunningState(@Context Configuration contextConfiguration, @Parameter(description = "The identifier of the room.", required = true) @PathParam("roomId") String roomId);

    @OldApiEndpoint(
            url = "/cxf/mcs-connect/rooms/${this.roomId}/recordingintermediate",
            description = "startRecording, extendRecording, stopRecording, chooseBooking"
    )
    @POST
    @Path("/rooms/{roomId}/recordingintermediate")
    @Operation(
            summary = "Set client-controlled (writable) aspects of the recording instance indirectly. bookingId is mandatory.",
            description =  "This call creates a new object that will be applied to the recording instance object. " +
                    "Note that it implements defaults if a value is not provided or when there is no recording instance set. " +
                    "The members of the RecordingInstanceIntermediate class correspond to the writable members of the RecordingInstance class."
    )
    @ApiResponse(responseCode = "200",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    //schema = @Schema(implementation = RecordingInstanceIntermediateDTO.class),
                    examples = {
                            @ExampleObject(
                                    name = "RecordingInstanceIntermediate",
                                    summary = "Settable fields",
                                    value="{ " +
                                            "\"bookingId\": \"1234567\"" +
                                            "\"recordingName\": \"2020-04-26 Session 26 - How to Write Json\" " +
                                            "\"recordingRunningState\": \"RECORDING\"" +
                                            "\"stopTime\": \"20200409T093225\"" +
                                            "}"
                            )
                    }
            )
    )
    @Produces(MediaType.APPLICATION_JSON)
    Response postRecordingInstanceIntermediate(@Context MessageContext context,
            @Parameter(description = "The identifier of the room that should be managed.", required = true) @PathParam("roomId") String roomId,
            RecordingInstanceIntermediatePost recordingInstanceIntermediatePost);


    // UNUSED BY CLIENT

    //@JsonView(Views.Public.class)
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    Response getApiHelp();

    @GET
    @Path("/version")
    @Operation( summary = "Get the application version." )
    Response getVersion();

    @GET
    @Path("/rooms/{roomId}")
    @Operation(summary = "Get information about the current state of a location.")
    Response getRoom(@Parameter(description = "The identifier of the room.", required = true) @PathParam("roomId") String providedLocationId);

    @GET
    @Path("/users/{userId}/bookings")
    @Operation(summary = "Get a list of permissible bookings at the time of the call.")
    Response getBookings(@Context MessageContext context, @Parameter(description = "The identifier of the user.", required = true) @PathParam("userId") String userId);
}
