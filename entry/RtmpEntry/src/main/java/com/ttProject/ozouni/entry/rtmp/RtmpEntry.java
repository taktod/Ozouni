package com.ttProject.ozouni.entry.rtmp;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.flazr.rtmp.client.ClientOptions;
import com.ttProject.ozouni.entry.ExtendedBasicParser;
import com.ttProject.ozouni.input.RtmpInputModule;

/**
 * rtmpの起動時の動作補助
 * @author taktod
 */
public class RtmpEntry {
	/** ロガー */
	private static Logger logger = Logger.getLogger(RtmpEntry.class);
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		// argに-DuniqueIdがある場合は環境変数と同じ扱いにしておきます。
		// optionを確認しておきます。
		ConfigurableApplicationContext context = null;
		try {
			CommandLine commandLine;
			ExtendedBasicParser parser = new ExtendedBasicParser();
			Options options = createOptions();
			String targetXml = "ozouni.xml";
			commandLine = parser.parse(options, args);
			if(commandLine.getOptions().length == 0) {
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
			if(commandLine.hasOption("xml")) {
				targetXml = commandLine.getOptionValue("xml");
			}
			List<String> argList = new ArrayList<String>();
			argList.addAll(parser.restArgs());
			argList.addAll(commandLine.getArgList());
			
			// ここまでこれたら問題ないので、起動します。(classPathで登録されているところにある、ozouni.xmlを読み込むことにします。)
			context = new ClassPathXmlApplicationContext(targetXml);
			
			RtmpInputModule rtmpInputModule = context.getBean(RtmpInputModule.class);
			ClientOptions clientOptions = rtmpInputModule.getClientOptions();
			if(!clientOptions.parseCli(argList.toArray(new String[]{}))) {
				return;
			}
			rtmpInputModule.start();
//			IEntry entry = context.getBean(IEntry.class);
//			entry.start(argList.toArray(new String[]{}));
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
		final Options options = new Options();
		// optionを作ります
		options.addOption(OptionBuilder.withArgName("id").hasArg(true).withDescription("set uniqueId for the process.").create("uniqueId"));
		options.addOption(OptionBuilder.withArgName("id").hasArg(true).withDescription("set targetId for connect.").create("targetId"));
		options.addOption(OptionBuilder.withArgName("xml").hasArg(true).withDescription("set xml path").create("xml"));
		return options;
	}
}
