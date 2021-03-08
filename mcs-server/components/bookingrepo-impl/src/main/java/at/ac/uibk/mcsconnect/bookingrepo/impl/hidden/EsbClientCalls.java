package at.ac.uibk.mcsconnect.bookingrepo.impl.hidden;

import javax.ws.rs.*;

@Produces( "application/xml" )
@Consumes( "application/xml" )
public interface EsbClientCalls {

    /**
     *
     * https://esb01.uibk.ac.at/cxf/vle-connect/sis/users/c102273/bookings?userGroupRoles=V&bookingsMinDate=&bookingsMaxDate=
     * @param userId c-Kennung
     * @param bookingsMinDate ISO 8601 Time
     * @param bookingsMaxDate ISO 8601 Time
     * @return
     */
    @GET
    @Produces("application/xml")
    @Path("/users/{userId}/mcsbookings")
    TvrBookings fetchBookingsForUserId(
            @PathParam("userId") String userId,
            @DefaultValue("V") @QueryParam("userGroupRoles") String roles, // NOTE TO SELF: JAX-RS ADDS = AFTER EACH QUERY PARAM
            @DefaultValue("") @QueryParam("bookingsMinDate") String bookingsMinDate,
            @DefaultValue("") @QueryParam("bookingsMaxDate") String bookingsMaxDate);
}