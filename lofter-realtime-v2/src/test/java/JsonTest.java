import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.pojo.pve.PveUserDialogue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

public class JsonTest {

    @BeforeEach
    void setUp() {}

    @Test
    public void testJsonNode() throws Exception {
        String roleGroupDialogue =
                "{\"userId\": 1234,\"createTime\": 1733206216174,\"targetRoleIds\": \"[{\\\"roleId\\\": 1, \\\"roleType\\\": 0}, {\\\"roleId\\\": 2, \\\"roleType\\\": 4}, {\\\"roleId\\\": 3, \\\"roleType\\\": 3}]\"}";
        String roleGroupDialogue2 =
                "{\"userId\": 1234,\"createTime\": 1733206216174,\"targetRoleIds\": \"[]\"}";

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(roleGroupDialogue2);
        System.out.println(jsonNode.get("userId").asLong());
        System.out.println(jsonNode.get("createTime").asLong());
        String targetRoleIds = jsonNode.get("targetRoleIds").asText();
        List<RoleInfo> targetRoleIdsList =
                objectMapper.readValue(targetRoleIds, new TypeReference<List<RoleInfo>>() {});
        if (!targetRoleIdsList.isEmpty()) {
            System.out.println(targetRoleIdsList.get(0).getRoleId());
            System.out.println(targetRoleIdsList.get(0).getRoleType());
        }
    }

    private static class RoleInfo {
        private Long roleId;
        private Integer roleType;

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }

        public Integer getRoleType() {
            return roleType;
        }

        public void setRoleType(Integer roleType) {
            this.roleType = roleType;
        }
    }

    @Test
    void testJsonNode2() throws Exception {
        Timestamp createTime = new Timestamp(System.currentTimeMillis());
        System.out.println(createTime);
        System.out.println(createTime.getTime());

        Date date = new Date(System.currentTimeMillis());
        System.out.println(date.getTime());

        Time time = new Time(System.currentTimeMillis());
        System.out.println(time.getTime());
    }

    @Test
    void testJsonNode3() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        String jsonString =
                "{\"tablename\":\"PVE_UserDialogue\",\"id\":2084385502,\"userid\":815612770,\"pveuserid\":2179481554,\"sender\":0,\"content\":\"行了啊你，（一把拍掉你的手，故意板着脸说道）别得寸进尺。（攻略值：4655）（整理了下被你弄乱的头发，耳根却有些泛红）（攻略值：4655）\",\"type\":0,\"audioflag\":0,\"ext\":null,\"createtime\":1733367633093,\"dbupdatetime\":1733367634000,\"requestid\":\"6a40c610-31ff-4689-94d2-9ac939080271\",\"status\":1,\"aisource\":6,\"sortno\":998,\"roleid\":17847170,\"roletype\":0,\"rolename\":\"周鹤眠\",\"messagetype\":0}";
        PveUserDialogue pveUserDialogue = objectMapper.readValue(jsonString, PveUserDialogue.class);
        System.out.println(objectMapper.writeValueAsString(pveUserDialogue));
    }
}
