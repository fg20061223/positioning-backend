package com.positioning.business.service;

import com.positioning.business.dto.RouteVO;
import com.positioning.business.entity.FloorConnect;
import com.positioning.business.entity.NavEdge;
import com.positioning.business.entity.NavNode;
import com.positioning.business.mapper.FloorConnectMapper;
import com.positioning.business.mapper.NavEdgeMapper;
import com.positioning.business.mapper.NavNodeMapper;
import com.positioning.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

/**
 * 路径规划服务: 基于导航图（nav_node + nav_edge + floor_connect）的 Dijkstra 最短路
 * 说明: 跨层连接按双向折算成本参与寻路; 转向指令可由前端依据节点坐标/边方位角生成
 */
@Service
@RequiredArgsConstructor
public class NavService {

    private final NavNodeMapper navNodeMapper;
    private final NavEdgeMapper navEdgeMapper;
    private final FloorConnectMapper floorConnectMapper;

    public RouteVO route(Long mallId, Long fromNodeId, Long toNodeId) {
        Map<Long, NavNode> nodeMap = navNodeMapper.selectAccessibleByMall(mallId).stream()
                .collect(Collectors.toMap(NavNode::getId, n -> n));
        if (!nodeMap.containsKey(fromNodeId)) {
            throw new BizException(404, "起点节点不存在或不可通行");
        }
        if (!nodeMap.containsKey(toNodeId)) {
            throw new BizException(404, "终点节点不存在或不可通行");
        }

        // 构建邻接表（含跨层连接, 双向）
        Map<Long, List<Edge>> adjacency = new HashMap<>();
        for (NavEdge e : navEdgeMapper.selectEnabledByMall(mallId)) {
            double cost = (e.getDistanceM() == null ? 0 : e.getDistanceM())
                    * (e.getWeight() == null ? 1 : e.getWeight());
            adjacency.computeIfAbsent(e.getFromNodeId(), k -> new ArrayList<>())
                    .add(new Edge(e.getFromNodeId(), e.getToNodeId(), cost, e.getId(), e.getEdgeType(), false));
            if (Boolean.TRUE.equals(e.getBidirectional())) {
                adjacency.computeIfAbsent(e.getToNodeId(), k -> new ArrayList<>())
                        .add(new Edge(e.getToNodeId(), e.getFromNodeId(), cost, e.getId(), e.getEdgeType(), false));
            }
        }
        for (FloorConnect fc : floorConnectMapper.selectEnabledByMall(mallId)) {
            double cost = fc.getCostM() == null ? 20 : fc.getCostM();
            adjacency.computeIfAbsent(fc.getFromNodeId(), k -> new ArrayList<>())
                    .add(new Edge(fc.getFromNodeId(), fc.getToNodeId(), cost, fc.getId(), fc.getConnectType(), true));
            adjacency.computeIfAbsent(fc.getToNodeId(), k -> new ArrayList<>())
                    .add(new Edge(fc.getToNodeId(), fc.getFromNodeId(), cost, fc.getId(), fc.getConnectType(), true));
        }

        // Dijkstra 最短路
        Map<Long, Double> dist = new HashMap<>();
        Map<Long, Edge> prev = new HashMap<>();
        PriorityQueue<Map.Entry<Long, Double>> queue =
                new PriorityQueue<>(Comparator.comparingDouble(Map.Entry::getValue));
        dist.put(fromNodeId, 0.0);
        queue.offer(new AbstractMap.SimpleEntry<>(fromNodeId, 0.0));

        while (!queue.isEmpty()) {
            Map.Entry<Long, Double> current = queue.poll();
            Long nodeId = current.getKey();
            double distance = current.getValue();
            if (distance > dist.getOrDefault(nodeId, Double.MAX_VALUE)) {
                continue;
            }
            if (nodeId.equals(toNodeId)) {
                break;
            }
            for (Edge edge : adjacency.getOrDefault(nodeId, List.of())) {
                double next = distance + edge.cost;
                if (next < dist.getOrDefault(edge.to, Double.MAX_VALUE)) {
                    dist.put(edge.to, next);
                    prev.put(edge.to, edge);
                    queue.offer(new AbstractMap.SimpleEntry<>(edge.to, next));
                }
            }
        }

        if (!dist.containsKey(toNodeId)) {
            throw new BizException(404, "起终点之间无可用路径");
        }

        // 回溯路径
        List<Long> nodeIds = new ArrayList<>();
        List<RouteVO.RouteSegment> segments = new ArrayList<>();
        Long cursor = toNodeId;
        nodeIds.add(cursor);
        while (prev.containsKey(cursor)) {
            Edge edge = prev.get(cursor);
            RouteVO.RouteSegment segment = new RouteVO.RouteSegment();
            segment.setFromNodeId(edge.from);
            segment.setToNodeId(edge.to);
            segment.setEdgeId(edge.edgeId);
            segment.setEdgeType(edge.edgeType);
            segment.setCrossFloor(edge.crossFloor);
            segment.setDistanceM(edge.cost);
            segments.add(segment);
            cursor = edge.from;
            nodeIds.add(cursor);
        }
        Collections.reverse(nodeIds);
        Collections.reverse(segments);

        RouteVO vo = new RouteVO();
        vo.setNodeIds(nodeIds);
        vo.setSegments(segments);
        vo.setTotalDistanceM(dist.get(toNodeId));
        return vo;
    }

    /** 邻接边 */
    record Edge(Long from, Long to, double cost, Long edgeId, String edgeType, boolean crossFloor) {
    }
}
