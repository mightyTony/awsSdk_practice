package com.example.awssdk_practice.s3.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.mediaconvert.AWSMediaConvert;
import com.amazonaws.services.mediaconvert.AWSMediaConvertClientBuilder;
import com.amazonaws.services.mediaconvert.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class AwsS3Service {

    // AWS 자격 증명 설정

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKeyId;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretAccessKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    // 입력 파일 버킷

    // s3://bbages/media/BigBuckBunny.mp4

    @Value("${input.s3.url}")
    private String inputS3Path;

    @Value("${input.s3.fileName}")
    private String inputFileName;

    // 출력 파일 버킷

    @Value("${output.s3.url}")
    private String outputS3Path;

    //mediaConvert

    @Value("${mediaconvert.endpoint}")
    private String mcEndPoint;

    @Value("${aws.roleARN}")
    private String roleARN;

    AWSMediaConvert awsMediaConvert;

    //권한 주입
    @PostConstruct
    public void setAwsMediaConvert(){
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKeyId, this.secretAccessKey);
        AwsClientBuilder.EndpointConfiguration eConf = new AwsClientBuilder.EndpointConfiguration(mcEndPoint, region);
        awsMediaConvert = AWSMediaConvertClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(eConf)
                .build();
    }

    public CreateJobResult beginJob(){

        JobSettings settings = createDashJobSetting();
        CreateJobResult result = convert(settings);

        return result;
    }

    public CreateJobResult convert(JobSettings settings){
        CreateJobRequest jobParam = new CreateJobRequest()
                .withRole(roleARN)
                .withSettings(settings);

        CreateJobResult mcResponse = awsMediaConvert.createJob(jobParam);
        return mcResponse;
    }

    public JobSettings createDashJobSetting(){

        // 작업 클릭
        JobSettings settings = new JobSettings();

        // Input 설정
        Input inputSource = new Input();
        inputSource.withFileInput(inputS3Path + inputFileName);
        AudioSelector as = new AudioSelector();
        as.withOffset(0);
        as.withProgramSelection(1);
        as.withDefaultSelection("DEFAULT");

        inputSource.addAudioSelectorsEntry("Audio Selector 1", as);
        settings.withInputs(inputSource);

        settings.withOutputGroups(generateDashOutput(outputS3Path));

        return settings;
    }

    public OutputGroup generateDashOutput(String output){

        OutputGroup og = new OutputGroup();
        OutputGroupSettings ogs = new OutputGroupSettings();

        DashIsoGroupSettings dgs = new DashIsoGroupSettings();
        dgs.withDestination(outputS3Path);
        dgs.withSegmentControl(DashIsoSegmentControl.SEGMENTED_FILES);
        dgs.withSegmentLength(10);
        dgs.withFragmentLength(2);

        ogs.withType(OutputGroupType.DASH_ISO_GROUP_SETTINGS)
                .withDashIsoGroupSettings(dgs);

        og.withName("OTT").withCustomName("dash")
                        .withOutputGroupSettings(ogs);

        og.withOutputs(createOTTOutput());

        return og;
    }

    private Output createOTTOutput() {

        // vbr, 해상도 1920 1080, 단일패스, 8mbps~12mbps, 비율 16:9

        H264Settings h264Settings = new H264Settings();

        h264Settings
                .withRateControlMode(H264RateControlMode.VBR)
                .withQualityTuningLevel(H264QualityTuningLevel.SINGLE_PASS)
                .withCodecLevel(H264CodecLevel.AUTO)
                .withCodecProfile(H264CodecProfile.HIGH)
                .withMaxBitrate(12288000)
                .withBitrate(8192000);

        VideoDescription vds = createVideoDescription(h264Settings);
        AudioDescription ads = createAudioDescription();

        Output op = new Output();
        op.withContainerSettings(new ContainerSettings().withContainer(ContainerType.MPD).withMpdSettings(new MpdSettings()))
                .withVideoDescription(vds)
                .withAudioDescriptions(ads);

        return op;
    }


    private AudioDescription createAudioDescription() {

        AudioDescription ads = new AudioDescription();

        AudioCodecSettings acs = new AudioCodecSettings();

        acs.withCodec(AudioCodec.AAC)
                .withAacSettings(
                        new AacSettings()
                                .withBitrate(256000)
                                .withSampleRate(48000)
                                .withCodingMode(AacCodingMode.CODING_MODE_2_0)
                                .withRawFormat(AacRawFormat.NONE)
                                .withSpecification(AacSpecification.MPEG4)
                );

        return ads.withCodecSettings(acs);
    }

    private VideoDescription createVideoDescription(H264Settings h264Settings) {

        VideoCodecSettings vcs = new VideoCodecSettings();
        vcs.withCodec(VideoCodec.H_264)
                .withH264Settings(h264Settings);

        VideoDescription vds = new VideoDescription();
        vds.withWidth(1920);
        vds.withHeight(1080);
        vds.withCodecSettings(vcs);

        return vds;
    }
}









