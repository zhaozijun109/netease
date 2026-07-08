package com.netease.lofter.tango.api.client.deeplink;

import com.netease.lofter.tango.api.dto.deeplink.AdDeeplinkDTO;

import java.util.List;

public interface DeepLinkService {

    List<AdDeeplinkDTO> listAll();
}
