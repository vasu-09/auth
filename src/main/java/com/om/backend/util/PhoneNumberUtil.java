package com.om.backend.util;
import com.google.i18n.phonenumbers.Phonenumber;

/**
 * Utility helpers for working with phone numbers.
 */
public class PhoneNumberUtil {
    /** Assumes India. Converts 10-digit or +91 formats to E.164 (+91XXXXXXXXXX). */
    private static final com.google.i18n.phonenumbers.PhoneNumberUtil PHONE_UTIL =
            com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance();

    /**
     * Normalize an Indian mobile number into E.164 format (+91XXXXXXXXXX).
     * Accepts numbers with or without country code or plus sign and validates
     * them using Google's libphonenumber.
     *
     * @param raw raw user supplied phone number
     * @return formatted number in E.164 representation
     * @throws IllegalArgumentException when the number is not a valid Indian mobile
     */
    public static String toE164India(String raw) {
        try {
            Phonenumber.PhoneNumber parsed = PHONE_UTIL.parse(raw, "IN");
            if (!PHONE_UTIL.isValidNumberForRegion(parsed, "IN")) {
                throw new IllegalArgumentException("Invalid Indian mobile number: " + raw);
            }
            return PHONE_UTIL.format(parsed,
                    com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Indian mobile number: " + raw, e);
        }

    }
}
