package com.absolom.mix;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Hello world!
 *
 * @author PanZhe
 */
public class MixTextToJson {

    private static final String WORKSPACE_PATH = "D:/MixTextToJson";

    private static void traverseDirectory() throws IOException {
        File workspace = new File(MixTextToJson.WORKSPACE_PATH);
        if (!workspace.exists()) {
            throw new RuntimeException("工作空间不存在：D:/MixTextToJson");
        }
        LinkedList<File> stationList = new LinkedList<>(Arrays.asList(Objects.requireNonNull(workspace.listFiles())));
        if (CollectionUtils.isNotEmpty(stationList)) {
            for (File stationDirectory : stationList) {
                JSONObject content = new JSONObject();
                LinkedList<File> layerList = new LinkedList<>(Arrays.asList(Objects.requireNonNull(stationDirectory.listFiles())));
                if (CollectionUtils.isEmpty(layerList)) {
                    throw new RuntimeException("工作空间下未找到车站文件夹：D:/MixTextToJson/?/");
                }
                List<String> layerNameList = new ArrayList<>();
                for (File layerDirectory : layerList) {
                    if (layerDirectory.isDirectory()) {
                        layerNameList.add(layerDirectory.getName());
                        JSONObject layerObject = new JSONObject();
                        List<String> roomNameList = new ArrayList<>();
                        for (File roomItem : Objects.requireNonNull(layerDirectory.listFiles())) {
                            String roomName = roomItem.getName().replaceAll("[.][^.]+$", "");
                            roomNameList.add(roomName);
                            layerObject.put(roomName, readFileAndMixToJson(roomItem));
                        }
                        layerObject.put("nameKeys", roomNameList);
                        content.put(layerDirectory.getName(), layerObject);
                    }
                }
                if (CollectionUtils.isNotEmpty(layerNameList)) {
                    content.put("layer", layerNameList);
                }
                writeFile(WORKSPACE_PATH + "/" + stationDirectory.getName() + "/",
                        "flow_line_" + stationDirectory.getName() + ".json", content.toJSONString());
            }
        }
    }

    /**
     * 将文件内容拼接为指定格式的JSON
     * @param file
     * @return
     */
    private static List<JSONObject> readFileAndMixToJson(File file) {
        List<JSONObject> coordinateList = new ArrayList<>();
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            String lineStr;
            while (StringUtils.isNotEmpty((lineStr = bufferedReader.readLine()))) {
                String[] coordinateArray = lineStr.split(",");
                if (coordinateArray.length == 3) {
                    JSONObject coordinateItem = new JSONObject();
                    coordinateItem.put("x", coordinateArray[0]);
                    coordinateItem.put("y", coordinateArray[1]);
                    coordinateItem.put("z", coordinateArray[2]);
                    coordinateList.add(coordinateItem);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        coordinateList.add(coordinateList.get(0));
        return coordinateList;
    }

    /**
     * 输出txt
     * @param fileName 文件名
     * @param content 文件名
     * @throws IOException
     */
    private static void writeFile(String filePath, String fileName, String content) throws IOException {
        if (StringUtils.isBlank(content)) {
            throw new RuntimeException("Nothing can write.");
        }
        File file = new File(filePath + fileName);
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream outputStream = new FileOutputStream(filePath + fileName, true);
        outputStream.write(content.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();
        System.out.println("write success.");
    }


    public static void main(String[] args) throws IOException {
        traverseDirectory();
    }
}
