package zx.soft.spider.web.application;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zx.soft.spider.solr.domain.RecordInfo;
import zx.soft.spider.web.resource.SentiIndexResource;
import zx.soft.spider.web.sentiment.IndexingData;

public class SentiIndexApplication extends Application {

	private static Logger logger = LoggerFactory.getLogger(SentiIndexApplication.class);

	private final IndexingData indexingData;

	static Thread commitThread;

	public SentiIndexApplication() {
		indexingData = new IndexingData();
		/**
		 * 每分钟定时提交更新
		 */
		commitThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Timer timer = new Timer();
				timer.schedule(new TimerCommit(), 0, 60 * 1000);
			}
		});
		commitThread.start();
		logger.info("SolrCloud committed start ...");
	}

	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach("", SentiIndexResource.class);
		return router;
	}

	/**
	 * 添加索引数据
	 */
	public void addDatas(List<RecordInfo> records) {
		for (RecordInfo record : records) {
			indexingData.addData(record);
		}
	}

	/**
	 * 定时提交更新索引
	 */
	public class TimerCommit extends TimerTask {

		private final AtomicInteger count = new AtomicInteger(0);

		@Override
		public void run() {
			indexingData.commit();
			logger.info("SolrCloud committed " + count.addAndGet(1) + ".");
		}

	}

	public void close() {
		commitThread.interrupt(); // 可能需要修改
		indexingData.close();
	}

}
