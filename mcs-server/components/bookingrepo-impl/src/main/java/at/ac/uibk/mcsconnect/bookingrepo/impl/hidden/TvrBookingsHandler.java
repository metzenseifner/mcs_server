package at.ac.uibk.mcsconnect.bookingrepo.impl.hidden;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * UNUSED EXAMPLE OF SAX HANDLER
 * Registered handler with SAX for handling ESB responses.
 */
public class TvrBookingsHandler extends DefaultHandler { // ContentHandler

    private static final String MCSTVRBOOKINGS = "mcsTvrBookings";
    private static final String BOOKING = "booking";
    private static final String EXTERNALBOOKINGID = "externalBookingId";
    private static final String DATEFROM = "dateFrom";
    private static final String DATEUNTIL = "dateUntil";
    private static final String RESOURCEID = "resourceId";
    private static final String RESOURCENAME = "resourceName";
    private static final String LOCATIONNAME = "locationName";
    private static final String EXTERNALCOURSEID = "externalCourseId";
    private static final String EXTERNALGROUPID = "externalGroupId";
    private static final String TERMID = "termId";
    private static final String LVNR = "lvNr";
    private static final String LVTITLE = "lvTitle";
    private static final String GROUPID = "groupId";

    private String elementValue;
    private TvrBookings tvrBookings;

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        tvrBookings = new TvrBookings();
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        elementValue = new String(ch, start, length);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        switch (qName) {
            case MCSTVRBOOKINGS:
                tvrBookings.setBookings(new ArrayList<>());
            case BOOKING:
                tvrBookings.getBookings().add(new TvrBookingImpl());
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        switch(qName) {
            case EXTERNALBOOKINGID:
                latestTvrBooking().setExternalBookingId(elementValue);
            case DATEFROM:
                latestTvrBooking().setDateFrom(elementValue);
            case DATEUNTIL:
                latestTvrBooking().setDateUntil(elementValue);
            case RESOURCEID:
                latestTvrBooking().setResourceId(elementValue);
            case RESOURCENAME:
                latestTvrBooking().setResourceName(elementValue);
            case LOCATIONNAME:
                latestTvrBooking().setLocationName(elementValue);
            case EXTERNALCOURSEID:
                latestTvrBooking().setExternalCourseId(elementValue);
            case EXTERNALGROUPID:
                latestTvrBooking().setExternalGroupId(elementValue);
            case TERMID:
                latestTvrBooking().setTermId(elementValue);
            case LVNR:
                latestTvrBooking().setLvNr(elementValue);
            case LVTITLE:
                latestTvrBooking().setLvTitle(elementValue);
            case GROUPID:
                latestTvrBooking().setGroupId(elementValue);
        }
    }

    private TvrBookingImpl latestTvrBooking() {
        List<TvrBookingImpl> tvrBookingImplList = tvrBookings.getBookings();
        int latestBookingIndex = tvrBookingImplList.size() - 1;
        return tvrBookingImplList.get(latestBookingIndex);
    }

    public TvrBookings getTvrBookings() {
        return tvrBookings;
    }

}