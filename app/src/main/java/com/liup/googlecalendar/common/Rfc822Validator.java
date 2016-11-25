package com.liup.googlecalendar.common;

import android.text.TextUtils;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import android.widget.AutoCompleteTextView;

import java.util.regex.Pattern;

/**
 * Created by liupeng on 2016/11/16.
 */

public class Rfc822Validator implements AutoCompleteTextView.Validator {
	/*
	 * Regex.EMAIL_ADDRESS_PATTERN hardcodes the TLD that we accept, but we
	 * want to make sure we will keep accepting email addresses with TLD's
	 * that don't exist at the time of this writing, so this regexp relaxes
	 * that constraint by accepting any kind of top level domain, not just
	 * ".com", ".fr", etc...
	 */
	private static final Pattern EMAIL_ADDRESS_PATTERN =
			Pattern.compile("[^\\s@]+@[^\\s@]+\\.[a-zA-z][a-zA-Z][a-zA-Z]*");
	private String mDomain;
	/**
	 * Constructs a new validator that uses the specified domain name as
	 * the default when none is specified.
	 */
	public Rfc822Validator(String domain) {
		mDomain = domain;
	}
	/**
	 * {@inheritDoc}
	 */
	public boolean isValid(CharSequence text) {
		Rfc822Token[] tokens = Rfc822Tokenizer.tokenize(text);
		return tokens.length == 1 &&
				EMAIL_ADDRESS_PATTERN.
						matcher(tokens[0].getAddress()).matches();
	}
	/**
	 * @return a string in which all the characters that are illegal for the username
	 * or the domain name part of the email address have been removed.
	 */
	private String removeIllegalCharacters(String s) {
		StringBuilder result = new StringBuilder();
		int length = s.length();
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
            /*
             * An RFC822 atom can contain any ASCII printing character
             * except for periods and any of the following punctuation.
             * A local-part can contain multiple atoms, concatenated by
             * periods, so do allow periods here.
             */
			if (c <= ' ' || c > '~') {
				continue;
			}
			if (c == '(' || c == ')' || c == '<' || c == '>' ||
					c == '@' || c == ',' || c == ';' || c == ':' ||
					c == '\\' || c == '"' || c == '[' || c == ']') {
				continue;
			}
			result.append(c);
		}
		return result.toString();
	}
	/**
	 * {@inheritDoc}
	 */
	public CharSequence fixText(CharSequence cs) {
		// Return an empty string if the email address only contains spaces, \n or \t
		if (TextUtils.getTrimmedLength(cs) == 0) return "";
		Rfc822Token[] tokens = Rfc822Tokenizer.tokenize(cs);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tokens.length; i++) {
			String text = tokens[i].getAddress();
			int index = text.indexOf('@');
			if (index < 0) {
				// If there is no @, just append the domain of the account
				tokens[i].setAddress(removeIllegalCharacters(text) + "@" + mDomain);
			} else {
				// Otherwise, remove the illegal characters on both sides of the '@'
				String fix = removeIllegalCharacters(text.substring(0, index));
				String domain = removeIllegalCharacters(text.substring(index + 1));
				tokens[i].setAddress(fix + "@" + (domain.length() != 0 ? domain : mDomain));
			}
			sb.append(tokens[i].toString());
			if (i + 1 < tokens.length) {
				sb.append(", ");
			}
		}
		return sb;
	}
}
