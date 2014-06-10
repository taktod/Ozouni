package com.ttProject.ozouni.base.entry;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * メインエントリー
 * @author taktod
 */
public class MainEntry {
	/** ロガー */
	private static Logger logger = Logger.getLogger(MainEntry.class);
	/**
	 * エントリー
	 * @param args
	 */
	public static void main(String[] args) {
		// argに-DuniqueIdがある場合は環境変数と同じ扱いにしておきます。
		// optionを確認しておきます。
		Options options = createOptions();
		CommandLineParser parser = new BasicParser();
		CommandLine commandLine;
		ConfigurableApplicationContext context = null;
		try {
			commandLine = parser.parse(options, args);
			if(commandLine.getArgs().length == 0) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("test", options);
				return;
			}
			// オプションは他のモジュールで利用できるように、System.propertiesに登録しておきます。
			if(!commandLine.hasOption("uniqueId")) {
				throw new RuntimeException("uniqueIdが設定されていません。");
			}
			System.setProperty("uniqueId", commandLine.getOptionValue("uniqueId"));
			if(commandLine.hasOption("targetId")) {
				System.setProperty("targetId", commandLine.getOptionValue("targetId"));
			}
			// ここまでこれたら問題ないので、起動します。(classPathで登録されているところにある、ozouni.xmlを読み込むことにします。)
			context = new ClassPathXmlApplicationContext("ozouni.xml");
			IEntry entry = context.getBean(IEntry.class);
			entry.start(args);
		}
		catch(Exception e) {
			logger.fatal("起動に失敗しました", e);
		}
		finally {
			if(context != null) {
				context.close();
				context = null;
			}
		}
	}
	/**
	 * オプションをつくります。
	 * @return
	 */
	@SuppressWarnings("static-access")
	private static Options createOptions() {
		Options options = new Options();
		// optionを作ります
//		options.addOption("uniqId", false, "uniqueId33");
//		options.addOption(new Option("tes2t", "あいうえお"));
//		options.addOption(OptionBuilder.withArgName("test3333").hasArg().withDescription("test3ですよん").create("test3"));
		/*
usage: test
 -tes2t              あいうえお
 -test3 <test3333>   test3ですよん
 -uniqId             uniqueId33
		 */
		options.addOption(OptionBuilder.withArgName("id").hasArg().withDescription("set uniqueId for the process.").create("uniqueId"));
		options.addOption(OptionBuilder.withArgName("id").hasArg().withDescription("set targetId for connect.").create("targetId"));
		return options;
	}
}
