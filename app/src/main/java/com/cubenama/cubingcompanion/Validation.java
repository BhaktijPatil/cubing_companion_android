package com.cubenama.cubingcompanion;

public class Validation {



    // Function to check validity of WCA ID
    public boolean isWcaIdValid(String wcaId) {
        // ID has to be 10 characters long
        if (wcaId.length() != 10)
            return false;
        else {
            // Divide WCA ID into sub components
            String year = wcaId.substring(0, 4);
            String name = wcaId.substring(4, 8);
            String id = wcaId.substring(8, 10);

            // WCA ID FORMAT YYYY-ABCD-ID
            if (!year.matches("^[0-9]*$"))
                return false;
            if (!name.matches("^[A-Z]*$"))
                return false;
            return id.matches("^[0-9]*$");
        }
    }



    // Function to check validity of mobile number
    public boolean isMobileValid(String mobile)
    {
        // mobile number can only plus,dash and numbers
        if (!mobile.matches("^[-0-9+]*$"))
            return false;
        // ID has to be 10 characters long
        return mobile.length() >= 10;
    }



    // Function to check validity of name
    public boolean isNameValid(String name) {
        // Name can have all alphabetic characters & spaces
        return name.matches("^\\p{L}+[\\p{L}\\p{Pd}\\p{Zs}']*\\p{L}+$|^\\p{L}+$");
    }
}
