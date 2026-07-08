package com.netease.bdms.ndi.service.web.service.impl;

import com.netease.bdms.ndi.service.web.DataIntegrationWebTests;
import com.netease.bdms.ndi.service.web.service.ProjectConfigService;
import com.netease.bdms.ndi.service.web.util.ProjectConfigUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ProjectConfigServiceImplTest extends DataIntegrationWebTests {

  @Autowired
  private ProjectConfigService configService;
  @Autowired
  private ProjectConfigUtil configUtil;
  private static final String CONFIG_FILE = "src/main/conf/test/common.properties";

  @Test
  public void getConfig() {
    String key = "1";
    String value = configService.getConfig(key);
    System.out.println(value);

    System.out.println(configUtil.get("1"));

  }

  @Test
  public void setConfig() {
    Map<String, String> map = readCommonProperties();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      configService.setConfig(entry.getKey(), entry.getValue());
    }
    System.out.println(map);
  }

  private Map<String, String> readCommonProperties() {
    File file = new File("src/main/conf/online/common.properties");
    InputStream inputStream = null;
    BufferedReader bufferedReader = null;
    File out = new File("project.sql");
    OutputStream outputStream = null;
    Map<String, String> result = new HashMap<>();
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("INSERT INTO project_config(`config_key`, `config_value`, `namespace`) VALUES");
    try {
      inputStream = new FileInputStream(file);
      outputStream = new FileOutputStream(out);
      bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
      String line = null;
      while ((line = bufferedReader.readLine()) != null) {
        stringBuilder.append("(");
        String[] keyAndValue = line.split("=");
        if (keyAndValue.length == 2) {
          String key = keyAndValue[0].trim();
          String value = keyAndValue[1].trim();
          stringBuilder.append("'").append(key).append("',");
          stringBuilder.append("'").append(value).append("',");
          result.put(key, value);
        }
        stringBuilder.append("'NDI'),");
      }
      outputStream.write(stringBuilder.toString().getBytes());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      if (bufferedReader != null) {
        try {
          bufferedReader.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return result;
  }

  @Test
  public void generateProjectSQL() {
    File file = new File(CONFIG_FILE);
    InputStream inputStream = null;
    BufferedReader bufferedReader = null;
    File out = new File("project.sql");
    OutputStream outputStream = null;
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("INSERT INTO project_config(`config_key`, `config_value`, `namespace`) VALUES");
    try {
      inputStream = new FileInputStream(file);
      outputStream = new FileOutputStream(out);
      bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
      String line = null;
      while ((line = bufferedReader.readLine()) != null) {
        String[] keyAndValue = line.split("=");
        if (keyAndValue.length == 2) {
          stringBuilder.append("(");
          String key = keyAndValue[0].trim();
          String value = keyAndValue[1].trim();
          stringBuilder.append("'").append(key).append("',");
          stringBuilder.append("'").append(value).append("',");
          stringBuilder.append("'NDI'),");
        } else {
          continue;
        }
      }
      String sql = stringBuilder.toString().substring(0, stringBuilder.length() - 1);
      outputStream.write(sql.getBytes());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      if (bufferedReader != null) {
        try {
          bufferedReader.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

  }
}