package at.ac.uibk.mcsconnect.bookingrepo.impl.hidden;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class is used only to deserialize the XML from the ESB.
 * It get converted into a Collection in the {@see User} before
 * being serialized again into JSON.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mcsTvrBookings") // IMPORTANT FOR OUTER XML TAG
public class TvrBookings implements Iterable<TvrBookingImpl>{

    private static final Logger LOGGER = LoggerFactory.getLogger(TvrBookings.class);

    @XmlElement(name = "booking")
    private List<TvrBookingImpl> bookings = new ArrayList<>();

    public TvrBookings() {
        LOGGER.debug(String.format("%s container created using default empty constructor.", getClass().getSimpleName()));
    }


    public List<TvrBookingImpl> getBookings() {
        return new ArrayList<>(bookings);
    }

    public void setBookings(List<TvrBookingImpl> bookings) {
        this.bookings = bookings;
    }

    public void clear() {
        bookings.clear();
    }

    @JsonIgnore
    public Iterator<TvrBookingImpl> iterator() {
        return new BookingsIterator();
    }

    private class BookingsIterator implements Iterator<TvrBookingImpl> {

        @JsonIgnore
        private int position = 0;

        public boolean hasNext() {
            return (position < bookings.size()) ? true : false;
        }
        public TvrBookingImpl next() {
            return (this.hasNext()) ? bookings.get(position++) : null;
        }
        public void remove() {}
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(1024);
        for (TvrBookingImpl b : bookings) {
            sb.append(b);
        }
        return String.format("%s: %s", getClass().getSimpleName(), sb.toString());
    }
}