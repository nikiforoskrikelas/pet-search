package nk00322.surrey.petsearch.utils;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;
import com.mobsandgeeks.saripaar.Validator;

import java.util.ArrayList;

public class ValidationUtils {

    public final static String PASSWORD_FORMAT_ERROR = "Passwords must contain at least 8 characters, 1 capital, 1 lowercase and 1 number";

    public final static String EMAIL_REGEX = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"" +
            "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")" +
            "@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.)" +
            "{3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|" +
            "\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])";

    public final static String DESCRIPTION_CHAR_LIMIT = "^(.{1,300})$";
    public final static String TITLE_CHAR_LIMIT = "^(.{1,25})$";

    public static boolean areAllFieldsCompleted(EditText... textFields) {
        ArrayList<String> textFieldStrings = new ArrayList<>();
        for (EditText e : textFields)
            textFieldStrings.add(e.getText().toString());

        for (String s : textFieldStrings) {
            if (TextUtils.isEmpty(s)) {
                return false;
            }
        }
        return true;
    }

    //For clearing errors from TextInputEditText nested inside a TextInputLayout
    public static void clearTextInputEditTextErrors(EditText... textFields) {
        for (EditText e : textFields)
            ((TextInputLayout) e.getParent().getParent()).setError(null);
    }

    public static Validator setupTextInputLayoutValidator(Object controller, final View view) {
        Validator validator = new Validator(controller);

        validator.setViewValidatedAction(textInputLayoutView -> {
            if (textInputLayoutView instanceof TextInputLayout)
                ((TextInputLayout) view.findViewById(textInputLayoutView.getId()).getParent().getParent()).setError(null);
        });
        return validator;
    }
}
