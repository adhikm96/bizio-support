package com.thebizio.biziosupport.generator;

import com.thebizio.biziosupport.entity.Ticket;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.tuple.ValueGenerator;

import java.security.SecureRandom;
import java.util.Optional;

public class SecureRandomReferenceIdGenerator implements ValueGenerator<String> {

    private String prefix;

    private static final String CHAR_UPPER = "ABCDEFGHHJKLMNOPQRSTUVWXYZ";
    private static final String NUMBER = "0123456789";

    private static final String DATA_FOR_RANDOM_STRING = CHAR_UPPER + NUMBER;
    private static SecureRandom random = new SecureRandom();

     @Override
    public String generateValue(Session session, Object obj) {
        String randomNumber = generateRandomString(9);

        if (obj.getClass().getSimpleName().equals("Ticket")){
            prefix = "T";

            Query<Ticket> query = session.createQuery("from Ticket t where t.ticketRefNo=:rn", Ticket.class);
            query.setParameter("rn", prefix+randomNumber);

            Optional<Ticket> ticket = query.setHibernateFlushMode(FlushMode.COMMIT).uniqueResultOptional();

            if (ticket.isPresent()){
                    return generateValue(session,obj);
            }else {
                return prefix+randomNumber;
            }
        }
        return null;
    }

    public static String generateRandomString(int length) {
        if (length < 1) throw new IllegalArgumentException();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            // 0-62 (exclusive), random returns 0-61
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);
            sb.append(rndChar);
        }
        return sb.toString();
    }
}
