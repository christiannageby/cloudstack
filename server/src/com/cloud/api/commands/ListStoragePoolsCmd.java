/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.cloud.api.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.api.ApiDBUtils;
import com.cloud.api.BaseListCmd;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.StoragePoolResponse;
import com.cloud.dc.ClusterVO;
import com.cloud.storage.StoragePoolVO;
import com.cloud.storage.StorageStats;

@Implementation(method="searchForStoragePools", description="Lists storage pools.")
public class ListStoragePoolsCmd extends BaseListCmd {
    public static final Logger s_logger = Logger.getLogger(ListStoragePoolsCmd.class.getName());

    private static final String s_name = "liststoragepoolsresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name="clusterid", type=CommandType.LONG, description="list storage pools belongig to the specific cluster")
    private Long clusterId;

    @Parameter(name="ipaddress", type=CommandType.STRING, description="the IP address for the storage pool")
    private String ipAddress;

    @Parameter(name="name", type=CommandType.STRING, description="the name of the storage pool")
    private String storagePoolName;

    @Parameter(name="path", type=CommandType.STRING, description="the storage pool path")
    private String path;

    @Parameter(name="podid", type=CommandType.LONG, description="the Pod ID for the storage pool")
    private Long podId;

    @Parameter(name="zoneid", type=CommandType.LONG, description="the Zone ID for the storage pool")
    private Long zoneId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getClusterId() {
        return clusterId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getStoragePoolName() {
        return storagePoolName;
    }

    public String getPath() {
        return path;
    }

    public Long getPodId() {
        return podId;
    }

    public Long getZoneId() {
        return zoneId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getName() {
        return s_name;
    }

    @Override @SuppressWarnings("unchecked")
    public ListResponse<StoragePoolResponse> getResponse() {
        List<? extends StoragePoolVO> pools = (List<? extends StoragePoolVO>)getResponseObject();

        ListResponse<StoragePoolResponse> response = new ListResponse<StoragePoolResponse>();
        List<StoragePoolResponse> poolResponses = new ArrayList<StoragePoolResponse>();
        for (StoragePoolVO pool : pools) {
            StoragePoolResponse poolResponse = new StoragePoolResponse();
            poolResponse.setId(pool.getId());
            poolResponse.setName(pool.getName());
            poolResponse.setPath(pool.getPath());
            poolResponse.setIpAddress(pool.getHostAddress());
            poolResponse.setZoneId(pool.getDataCenterId());
            poolResponse.setZoneName(ApiDBUtils.findZoneById(pool.getDataCenterId()).getName());
            if (pool.getPoolType() != null) {
                poolResponse.setType(pool.getPoolType().toString());
            }
            if (pool.getPodId() != null) {
                poolResponse.setPodId(pool.getPodId());
                poolResponse.setPodName(ApiDBUtils.findPodById(pool.getPodId()).getName());
            }
            if (pool.getCreated() != null) {
                poolResponse.setCreated(pool.getCreated());
            }

            StorageStats stats = ApiDBUtils.getStoragePoolStatistics(pool.getId());
            long capacity = pool.getCapacityBytes();
            long available = pool.getAvailableBytes() ;
            long used = capacity - available;

            if (stats != null) {
                used = stats.getByteUsed();
                available = capacity - used;
            }

            poolResponse.setDiskSizeTotal(pool.getCapacityBytes());
            poolResponse.setDiskSizeAllocated(used);

            if (pool.getClusterId() != null) {
                ClusterVO cluster = ApiDBUtils.findClusterById(pool.getClusterId());
                poolResponse.setClusterId(cluster.getId());
                poolResponse.setClusterName(cluster.getName());
            }           

            poolResponse.setTags(ApiDBUtils.getStoragePoolTags(pool.getId()));

            poolResponse.setResponseName("storagepool");
            poolResponses.add(poolResponse);
        }

        response.setResponses(poolResponses);
        response.setResponseName(getName());
        return response;
    }
}
