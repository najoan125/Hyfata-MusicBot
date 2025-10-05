package com.jagrosh.jmusicbot.utils;
import com.jagrosh.jmusicbot.JMusicBot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AudioNormalizer {

    // FFmpeg 실행 파일 경로 (환경 변수 PATH에 등록되어 있다면 "ffmpeg"만으로 충분)
    private static final String FFMPEG_PATH = "ffmpeg";

    /**
     * 지정된 경로의 오디오 파일 음량을 평준화하여 새로운 파일로 저장합니다.
     *
     * @param inputFilePath  평준화할 원본 오디오 파일의 경로 (예: "source.mp3")
     * @param outputFilePath 평준화된 결과를 저장할 파일의 경로 (예: "normalized.mp3")
     * @throws IOException          파일 I/O 또는 FFmpeg 실행 오류 발생 시
     * @throws InterruptedException FFmpeg 프로세스가 중단될 경우
     */
    public static void normalizeLoudness(String inputFilePath, String outputFilePath) throws IOException, InterruptedException {
        File inputFile = new File(inputFilePath);
        if (!inputFile.exists()) {
            throw new IOException("입력 파일을 찾을 수 없습니다: " + inputFilePath);
        }
        File outputFile = new File(outputFilePath);

        // 1. 1차 실행: 오디오 음량 분석
        Map<String, String> loudnessStats = analyzeLoudness(inputFile);
        if (loudnessStats.isEmpty()) {
            throw new RuntimeException("FFmpeg를 통해 음량 통계를 분석하지 못했습니다.");
        }

        // 2. 2차 실행: 분석 결과를 바탕으로 평준화
        normalize(inputFile, outputFile, loudnessStats);

        try {
            Files.delete(Paths.get(inputFilePath));
        } catch (IOException ex) {
            JMusicBot.LOG.error("An error occurred while deleting MP3 TTS in AudioNormalizer", ex);
        }
    }

    /**
     * FFmpeg를 실행하여 오디오 파일의 음량 통계를 분석합니다. (1차 패스)
     *
     * @param inputFile 분석할 오디오 파일
     * @return 분석된 음량 통계 (Integrated, True Peak 등)가 담긴 Map
     */
    private static Map<String, String> analyzeLoudness(File inputFile) throws IOException, InterruptedException {
        // loudnorm 필터 옵션: 유튜브 뮤직 및 타 플랫폼을 고려하여 -9 LUFS로 설정합니다.
        // I=-7: 목표 통합 음량 -7 LUFS
        // TP=-0.1: 목표 True Peak -0.1 dBTP
        // LRA=1: 목표 음량 범위
        String[] command = {
                FFMPEG_PATH,
                "-hide_banner", // FFmpeg 버전 정보 등 생략
                "-i", inputFile.getAbsolutePath(),
                "-af", "loudnorm=I=-7:TP=-0.1:LRA=1:print_format=summary",
                "-f", "null",
                "-"
        };

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        Map<String, String> stats = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            Pattern pattern = Pattern.compile("\\s*Input\\s+(Integrated|True Peak|LRA|Threshold):\\s*([\\d.-]+).*");

            while ((line = reader.readLine()) != null) {
                // System.out.println("FFmpeg(분석): " + line); // 디버깅 필요 시 주석 해제
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    String key = matcher.group(1);
                    String value = matcher.group(2);
                    stats.put(key, value);
                }
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("FFmpeg 분석 프로세스가 비정상적으로 종료되었습니다. 종료 코드: " + exitCode);
        }
        return stats;
    }

    /**
     * 분석된 통계를 사용하여 오디오 파일을 평준화합니다. (2차 패스)
     *
     * @param inputFile  원본 오디오 파일
     * @param outputFile 결과물을 저장할 파일
     * @param stats      1차 분석에서 얻은 음량 통계
     */
    private static void normalize(File inputFile, File outputFile, Map<String, String> stats) throws IOException, InterruptedException {
        String measuredI = stats.get("Integrated");
        String measuredTP = stats.get("True Peak");
        String measuredLRA = stats.get("LRA");
        String measuredThresh = stats.get("Threshold");

        // 1차 분석에서 얻은 measured 값을 loudnorm 필터에 적용
        String afValue = String.format("loudnorm=I=-16:TP=-1.5:LRA=11:measured_I=%s:measured_TP=%s:measured_LRA=%s:measured_thresh=%s",
                measuredI, measuredTP, measuredLRA, measuredThresh);

        String[] command = {
                FFMPEG_PATH,
                "-hide_banner",
                "-i", inputFile.getAbsolutePath(),
                "-af", afValue,
                "-c:a", "libmp3lame", // MP3 오디오 코덱 지정
                "-b:a", "192k",      // 오디오 비트레이트 (192kbps)
                "-y", // 출력 파일이 이미 존재하면 덮어쓰기
                outputFile.getAbsolutePath()
        };

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        // FFmpeg 진행 상황을 보려면 아래 스트림 리더를 활성화하세요.
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while (reader.readLine() != null) {
//                 System.out.println("FFmpeg(평준화): " + reader.readLine()); // 디버깅 필요 시 주석 해제
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("FFmpeg 평준화 프로세스가 비정상적으로 종료되었습니다. 종료 코드: " + exitCode);
        }
    }

    // --- 메인 메소드 (테스트용) ---
//    public static void main(String[] args) {
//        try {
//            // 1. 평준화할 MP3 파일 경로를 지정합니다.
//            //    이 파일을 프로젝트 루트 디렉토리나 원하는 위치에 준비해야 합니다.
//            String inputFilePath = "input.mp3";
//
//            // 2. 평준화된 결과가 저장될 파일 경로를 지정합니다.
//            String outputFilePath = "output_normalized.mp3";
//
//            // 입력 파일 존재 여부 확인
//            File inputFile = new File(inputFilePath);
//            if (!inputFile.exists()) {
//                System.err.println("오류: 입력 파일 '" + inputFilePath + "'을(를) 찾을 수 없습니다.");
//                System.err.println("테스트를 위해 프로젝트 폴더에 'input.mp3' 파일을 위치시켜 주세요.");
//                return;
//            }
//
//            // 3. 음량 평준화 실행
//            AudioNormalizer.normalizeLoudness(inputFilePath, outputFilePath);
//
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
}