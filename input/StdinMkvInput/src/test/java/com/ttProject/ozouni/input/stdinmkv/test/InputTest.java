package com.ttProject.ozouni.input.stdinmkv.test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.apache.log4j.Logger;

import com.ttProject.container.IContainer;
import com.ttProject.container.mkv.MkvBlockTag;
import com.ttProject.container.mkv.MkvTagReader;
import com.ttProject.nio.channels.IReadChannel;

/**
 * 標準入力データをIReadChannelとして利用するにはどうすればよいか？テストを書いてみた。
 * @author taktod
 */
public class InputTest {
	/** ロガー */
	private Logger logger = Logger.getLogger(InputTest.class);
//	@Test
	public void test() throws Exception {
		StringBuilder command = new StringBuilder();
//		command.append("java -Dfile.encoding=UTF-8 -cp Ozouni/input/StdinMkvInput/target/test-classes/ com.ttProject.ozouni.input.stdinmkv.test.UpdateProgram");
//		ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", command.toString());
		command.append("cat ahiru.mkv");
		ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", command.toString());
		Process p = pb.start();
		logger.info(p);
		InputStream is = p.getInputStream();
		IReadChannel channel = new ISChannel(is);
		MkvTagReader reader = new MkvTagReader();
		IContainer container = null;
		while((container = reader.read(channel)) != null) {
			if(container instanceof MkvBlockTag) {
				MkvBlockTag blockTag = (MkvBlockTag) container;
				logger.info(blockTag.getFrame());
			}
		}
	}
	public static class ISChannel implements IReadChannel {
		private ReadableByteChannel channel;
		private int pos = 0;
		public ISChannel(InputStream is) {
			channel = Channels.newChannel(is);
		}
		@Override
		public boolean isOpen() {
			return true;
		}

		@Override
		public int size() throws IOException {
			return Integer.MAX_VALUE;
		}

		@Override
		public int position() throws IOException {
			return pos;
		}

		@Override
		public IReadChannel position(int newPosition) throws IOException {
			if(newPosition > pos) {
				// とりあえず読みすてで対処しよう。
				try {
					ByteBuffer buf = ByteBuffer.allocate(newPosition - pos);
					while(newPosition > pos) {
						read(buf);
						Thread.sleep(10);
					}
//					BufferUtil.safeRead(this, newPosition - pos);
				}
				catch(Exception e) {
					
				}
			}
			else if(newPosition != pos) {
				throw new RuntimeException("位置の移動は禁止です。:" + pos + " / " + newPosition);
			}
			return this;
		}

		@Override
		public int read(ByteBuffer dst) throws IOException {
			int pos = dst.position();
			channel.read(dst);
			this.pos += dst.position() - pos;
			return dst.position() - pos;
		}

		@Override
		public void close() throws IOException {
			
		}
	}
}
