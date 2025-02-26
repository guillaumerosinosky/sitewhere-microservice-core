/**
 * Copyright © 2014-2021 The SiteWhere Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sitewhere.microservice.kafka;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.sitewhere.microservice.lifecycle.TenantEngineLifecycleComponent;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.microservice.kafka.IMicroserviceKafkaConsumer;
import com.sitewhere.spi.microservice.lifecycle.ILifecycleProgressMonitor;

/**
 * Base class for components that consume messages from a Kafka topic.
 */
public abstract class MicroserviceKafkaConsumer extends TenantEngineLifecycleComponent
	implements IMicroserviceKafkaConsumer {

    /** Consumer */
    private KafkaConsumer<String, byte[]> consumer;

    /** Executor service */
    private ExecutorService executor;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.server.lifecycle.LifecycleComponent#start(com.sitewhere.spi
     * .server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void start(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	getLogger().info("Consumer connecting to Kafka: " + KafkaUtils.getBootstrapServers(getMicroservice()));
	getLogger().info("Will be consuming messages from: " + getSourceTopicNames());
	this.consumer = new KafkaConsumer<>(buildConfiguration());
	this.executor = Executors.newSingleThreadExecutor(new MicroserviceConsumerThreadFactory());
	executor.execute(new MessageConsumer());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.server.lifecycle.LifecycleComponent#stop(com.sitewhere.spi.
     * server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void stop(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	if (getConsumer() != null) {
	    getConsumer().wakeup();
	}
	if (executor != null) {
	    executor.shutdown();
	}
    }

    /**
     * Build configuration settings used by Kafka streams.
     * 
     * @return
     * @throws SiteWhereException
     */
    protected Properties buildConfiguration() throws SiteWhereException {
	Properties config = new Properties();
	config.put(ConsumerConfig.CLIENT_ID_CONFIG, getConsumerId());
	config.put(ConsumerConfig.GROUP_ID_CONFIG, getConsumerGroupId());
	config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaUtils.getBootstrapServers(getMicroservice()));
	config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
	config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
	config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
	config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
	config.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 1000);
	return config;
    }

    /*
     * @see
     * com.sitewhere.spi.microservice.kafka.IMicroserviceKafkaConsumer#getConsumer()
     */
    @Override
    public KafkaConsumer<String, byte[]> getConsumer() {
	return consumer;
    }

    protected void setConsumer(KafkaConsumer<String, byte[]> consumer) {
	this.consumer = consumer;
    }

    /**
     * Thread that polls Kafka for records arriving on the specified topic.
     */
    private class MessageConsumer implements Runnable {

	@Override
	public void run() {
	    // Attempt to subscribe
	    while (true) {
		try {
		    getLogger()
			    .debug(String.format("Kafka consumer subscribing to %s", getSourceTopicNames().toString()));
		    getConsumer().subscribe(getSourceTopicNames());
		    break;
		} catch (SiteWhereException e) {
		    getLogger().error("Unable to subscribe to topics.", e);
		} catch (Throwable e) {
		    getLogger().error("Unhandled exception while subscribing to topics.", e);
		}
		try {
		    Thread.sleep(1000);
		} catch (InterruptedException e) {
		    return;
		}
	    }
	    try {
		while (true) {
		    ConsumerRecords<String, byte[]> records = getConsumer().poll(Duration.ofMillis(Long.MAX_VALUE));
		    getLogger().debug(String.format("Kafka consumer received %d records on poll.", records.count()));
		    for (TopicPartition topicPartition : records.partitions()) {
			try {
			    List<ConsumerRecord<String, byte[]>> topicRecords = records.records(topicPartition);
			    getLogger().debug(String.format("Kafka consumer processing %d records for %s partition %s.",
				    topicRecords.size(), topicPartition.topic(), topicPartition.partition()));
			    process(topicPartition, topicRecords);
			} catch (Throwable e) {
			    getLogger().error("Unhandled exception in consumer processing.", e);
			}
		    }
		}
	    } catch (WakeupException e) {
		getLogger().info("Consumer thread received shutdown request.");
		getConsumer().unsubscribe();
	    } finally {
		getConsumer().close();
	    }
	}
    }

    /** Used for naming microservice consumer thread */
    private class MicroserviceConsumerThreadFactory implements ThreadFactory {

	/** Counts threads */
	private AtomicInteger counter = new AtomicInteger();

	public Thread newThread(Runnable r) {
	    return new Thread(r, "Kafka Consumer " + counter.incrementAndGet());
	}
    }
}