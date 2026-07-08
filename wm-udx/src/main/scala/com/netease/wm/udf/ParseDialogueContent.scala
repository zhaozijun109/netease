package com.netease.wm.udf

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.hadoop.hive.ql.exec.{Description, UDF}

@Description(name="ParseDialogueContent", value="parse pve dialogue content")
class ParseDialogueContent extends UDF {
  private val objectMapper = new ObjectMapper()

  def evaluate(aiSource: String, aiResponse: String): String = {
    try {
      val rawJson = if (aiResponse.trim.startsWith("\"") && aiResponse.trim.endsWith("\"")) {
        objectMapper.readValue(aiResponse, classOf[String])
      } else {
        aiResponse
      }

      val root = objectMapper.readTree(rawJson)

      aiSource match {
        case "PVE_YUYAN_PLUS" => root.get("output_text").asText()
        case "DOU_BAO_0612" | "DOU_BAO_1215" | "PVE_DOU_BAO" | "DOU_BAO_0529" =>
          if (rawJson.split("\\|").length >= 2) rawJson.split("\\|")(1).substring("content:".length) else root.get("choices").get(0).get("message").get("content").asText()
        case "PVE_GLOW_AI" => root.get("choices").get(0).get("text").asText()
        case "PVE_AI_LAB" | "PVE_AI_LAB_V2" => root.get("reply").asText()
        case "PVE_CHAT_BOT" => root.get("reply").asText()
        case "SenseChat-Character-Pro" | "SenseChat-Character-Pro-Q" => root.get("data").get("reply").asText()
        case "TONGYI_XINGCHEN" | "TONGYI_XINGCHEN_0819" | "TONGYI_XINGCHEN_0723" => root.get("choices").get(0).get("message").get("content").asText()
        case "PVE_MM_CCV2" => root.get("choices").get(0).get("message").get("content").asText()
        case "unknown" => root.get("reply").asText()
        case _ => rawJson
      }
    } catch {
      case _: Exception => aiResponse
    }
  }
}

object test {
  val objectMapper = new ObjectMapper()

  def evaluate(aiSource: String, aiResponse: String): String = {
    try {
      val rawJson = if (aiResponse.trim.startsWith("\"") && aiResponse.trim.endsWith("\"")) {
        objectMapper.readValue(aiResponse, classOf[String])
      } else {
        aiResponse
      }

      val root = objectMapper.readTree(rawJson)

      aiSource match {
        case "PVE_YUYAN_PLUS" => root.get("output_text").asText()
        case "DOU_BAO_0612" | "DOU_BAO_1215" | "PVE_DOU_BAO" | "DOU_BAO_0529" =>
          if (rawJson.split("\\|").length >= 2) rawJson.split("\\|")(1).substring("content:".length) else root.get("choices").get(0).get("message").get("content").asText()
        case "PVE_GLOW_AI" => root.get("choices").get(0).get("text").asText()
        case "PVE_AI_LAB" | "PVE_AI_LAB_V2" => root.get("reply").asText()
        case "PVE_CHAT_BOT" => root.get("reply").asText()
        case "SenseChat-Character-Pro" | "SenseChat-Character-Pro-Q" => root.get("data").get("reply").asText()
        case "TONGYI_XINGCHEN" | "TONGYI_XINGCHEN_0819" | "TONGYI_XINGCHEN_0723" => root.get("choices").get(0).get("message").get("content").asText()
        case "PVE_MM_CCV2" => root.get("choices").get(0).get("message").get("content").asText()
        case "unknown" => root.get("reply").asText()
        case _ => rawJson
      }
    } catch {
      case _: Exception => aiResponse
    }
  }

  def main(args: Array[String]): Unit = {
    val s1 = "\"traceId:6dc9fb4ea1044c9a935d6d34d9e81853|content:（小心地将你扶起，让你靠在自己身上）走吧，大夫就在前面的镇子上，再坚持一下。（起身带着你向前走去）|usage:{\\\"promptTokens\\\":3331,\\\"completionTokens\\\":34,\\\"totalTokens\\\":3365,\\\"promptTokensDetails\\\":{\\\"cachedTokens\\\":0},\\\"completionTokensDetails\\\":{\\\"reasoningTokens\\\":0}}\""
    println(evaluate("DOU_BAO_0529", s1))

    val s2 = "\"{\\\"data\\\":{\\\"id\\\":\\\"ff06cdca-b7b3-47ba-b216-bb788723868e\\\",\\\"reply\\\":\\\"（靠在墙边，仿佛失去了所有力气）父亲说得对，我确实...不够好。（闭上眼睛）即使现在取得了一些成就，我依然感觉到不安。（睁开眼，目光中带着祈求）但你的话...让我看到了一些不一样的可能。\\\",\\\"usage\\\":{\\\"prompt_tokens\\\":2391,\\\"completion_tokens\\\":80,\\\"total_tokens\\\":2471,\\\"knowledge_tokens\\\":0},\\\"choices\\\":[{\\\"index\\\":0,\\\"message\\\":\\\"（靠在墙边，仿佛失去了所有力气）父亲说得对，我确实...不够好。（闭上眼睛）即使现在取得了一些成就，我依然感觉到不安。（睁开眼，目光中带着祈求）但你的话...让我看到了一些不一样的可能。\\\",\\\"finish_reason\\\":\\\"stop\\\"}]}}\""
    println(evaluate("SenseChat-Character-Pro", s2))

    val s3 = "\"{\\\"choices\\\":[{\\\"message\\\":{\\\"content\\\":\\\"中餐的话……（拿出手机搜索了一下）离我们不远有家粤菜馆，评价还挺高的，环境也安静，你觉得可以吗？\\\",\\\"role\\\":\\\"assistant\\\"},\\\"finish_reason\\\":\\\"stop\\\",\\\"index\\\":0,\\\"logprobs\\\":null}],\\\"object\\\":\\\"chat.completion\\\",\\\"usage\\\":{\\\"prompt_tokens\\\":1489,\\\"completion_tokens\\\":31,\\\"total_tokens\\\":1520,\\\"prompt_tokens_details\\\":{\\\"cached_tokens\\\":1280}},\\\"created\\\":1761582964,\\\"system_fingerprint\\\":null,\\\"model\\\":\\\"qwen-plus-character\\\",\\\"id\\\":\\\"chatcmpl-e349dcdc-22ed-49c3-a503-04adf03e1c26\\\"}\""
    println(evaluate("TONGYI_XINGCHEN_0723", s3))

    val s4 = "{\"id\":\"02176086934493358165abd7c65eb0429fee971eb4288986372b7\",\"object\":\"chat.completion\",\"created\":1760869345,\"model\":\"\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"ASSISTANT\",\"content\":\" （时南深从后面搂住你，下巴抵在你的肩上）宝贝，你只能这样看着我，你的眼里不许有其他人。\"},\"finishReason\":\"stop\"}],\"usage\":{\"promptTokens\":565,\"completionTokens\":30,\"totalTokens\":595}}"
    println(evaluate("PVE_DOU_BAO", new ObjectMapper().readTree(s4).get("choices").get(0).get("message").get("content").asText()))

  }

}