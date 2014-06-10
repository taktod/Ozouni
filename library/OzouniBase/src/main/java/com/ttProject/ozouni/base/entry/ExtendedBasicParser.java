package com.ttProject.ozouni.base.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.ParseException;

public class ExtendedBasicParser extends BasicParser {
	private List<String> restArgsList = new ArrayList<String>();
	@SuppressWarnings("rawtypes")
	@Override
	protected void processOption(String arg, ListIterator iter)
			throws ParseException {
		boolean hasOption = getOptions().hasOption(arg);
		if(hasOption) {
			super.processOption(arg, iter);
		}
		else {
			restArgsList.add(arg);
			Object next = iter.next();
			if(next.toString().startsWith("-")) {
				iter.previous();
			}
			else {
				restArgsList.add(next.toString());
			}
		}
	}
	public String[] restArgs() {
		return restArgsList.toArray(new String[]{});
	}
}
