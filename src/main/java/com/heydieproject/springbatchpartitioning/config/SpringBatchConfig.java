package com.heydieproject.springbatchpartitioning.config;


import com.heydieproject.springbatchpartitioning.config.partitioner.ColumnPartitioner;
import com.heydieproject.springbatchpartitioning.entity.CarPark;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j
public class SpringBatchConfig {

    private final CarParkWriter carParkWriter;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobBuilderFactory jobBuilderFactory;

    @Bean
    public FlatFileItemReader<CarPark> reader() {
        FlatFileItemReader<CarPark> carParkFlatFileItemReader = new FlatFileItemReader<>();
        carParkFlatFileItemReader.setResource(new FileSystemResource("src/main/resources/HDBCarparkInformation.csv"));
        carParkFlatFileItemReader.setName("csvReader");
        carParkFlatFileItemReader.setLinesToSkip(1);
        carParkFlatFileItemReader.setLineMapper(lineMapper());

        return carParkFlatFileItemReader;
    }

    private LineMapper<CarPark> lineMapper() {
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("carParkNo","address","xCoord","yCoord","carParkType","typeOfParkingSystem","shortTermParking","freeParking","nightParking","carParkDecks","gantryHeight","carParkBasement");

        BeanWrapperFieldSetMapper<CarPark> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CarPark.class);

        DefaultLineMapper<CarPark> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    @Bean
    public CarParkProcessor processor() {
        return new CarParkProcessor();
    }

    @Bean
    public PartitionHandler partitionHandler() {
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setGridSize(4);
        partitionHandler.setTaskExecutor(taskExecutor());
        partitionHandler.setStep(slaveStep());

        return partitionHandler;
    }

    @Bean
    public Step slaveStep() {
        return stepBuilderFactory.get("slaveStep").<CarPark,CarPark>chunk(100)
                .reader(reader())
                .processor(processor())
                .writer(carParkWriter)
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(4);
        taskExecutor.setCorePoolSize(4);
        taskExecutor.setQueueCapacity(20);
        return taskExecutor;
    }

    @Bean
    public Step masterStep() {
        return stepBuilderFactory.get("masterStep")
                .partitioner(slaveStep().getName(), columnPartitioner())
                .partitionHandler(partitionHandler())
                .build();
    }

    @Bean
    public Job runJob() {
        return jobBuilderFactory.get("upload")
                .flow(masterStep()).end().build();
    }

    @Bean
    public ColumnPartitioner columnPartitioner() {
        return new ColumnPartitioner();
    }
}
