<template>
  <div class="container">
    <div class="header">
      <div>
        <h2>兑换与奖励</h2>
        <div class="notice">可兑换奖励与兑换记录</div>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-title">可兑换奖励</div>
        <div class="stat-value">{{ rewards.length }}</div>
        <div class="stat-foot">奖励库存</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">兑换记录</div>
        <div class="stat-value">{{ redemptions.length }}</div>
        <div class="stat-foot">历史兑换次数</div>
      </div>
    </div>

    <div class="card">
      <h3>可兑换奖励</h3>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th style="width: 44px;">选择</th>
              <th style="width: 60px;">序号</th>
              <th>奖励</th>
              <th>所需积分</th>
              <th>库存</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(reward, index) in rewards" :key="reward.id">
              <td><input type="checkbox" style="width: auto;" /></td>
              <td>{{ index + 1 }}</td>
              <td>{{ reward.name }}</td>
              <td>{{ reward.pointsCost }}</td>
              <td>{{ reward.stock }}</td>
              <td>
                <VeaButton class="btn-sm info" v-if="can('child.reward.redeem')" @click="redeem(reward.id)">兑换</VeaButton>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="notice" style="margin-top: 8px;">兑换后需要家长审核</div>
    </div>

    <div class="card">
      <h3>兑换记录</h3>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th style="width: 44px;">选择</th>
              <th style="width: 60px;">序号</th>
              <th>奖励</th>
              <th>积分</th>
              <th>状态</th>
              <th>审核备注</th>
              <th>审核时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(record, index) in redemptions" :key="record.id">
              <td><input type="checkbox" style="width: auto;" /></td>
              <td>{{ index + 1 }}</td>
              <td>{{ record.rewardName }}</td>
              <td>{{ record.pointsCost }}</td>
              <td>{{ record.status }}</td>
              <td>{{ record.reviewNote || '-' }}</td>
              <td>{{ formatDateTime(record.reviewedAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import { can } from '../permissions'
import VeaButton from '../components/VeaButton.vue'

const rewards = ref([])
const redemptions = ref([])

const loadAll = async () => {
  const [rewardRes, redemptionRes] = await Promise.all([
    api.get('/child/rewards'),
    api.get('/child/redemptions')
  ])
  rewards.value = rewardRes.data
  redemptions.value = redemptionRes.data
}

const redeem = async (id) => {
  await api.post(`/child/rewards/${id}/redeem`)
  await loadAll()
}

const formatDateTime = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

onMounted(loadAll)
</script>

