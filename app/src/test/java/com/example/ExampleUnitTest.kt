package com.example

import com.example.domain.intelligence.NluEngine
import com.example.domain.intelligence.TaskPlanner
import com.example.domain.models.ActionType
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  private val nluEngine = NluEngine()
  private val taskPlanner = TaskPlanner(nluEngine)

  @Test
  fun testCompoundSplitting() {
    val query = "Turn off wifi, turn off bluetooth and set an alarm for 6:30"
    val parts = nluEngine.splitCompoundQuery(query)
    assertEquals(3, parts.size)
  }

  @Test
  fun testCallAndTextCompoundPattern() {
    val query = "Call Rahul and tell him I'll arrive in 20 minutes"
    val parts = nluEngine.splitCompoundQuery(query)
    assertEquals(2, parts.size)
    assertTrue(parts[0].contains("Text Rahul"))
    assertTrue(parts[1].contains("Call Rahul"))
  }

  @Test
  fun testTaskPlanGeneration() {
    val query = "Set a 15 minute timer"
    val plan = taskPlanner.planTask(query)
    assertEquals(1, plan.steps.size)
    assertEquals(ActionType.SET_TIMER, plan.steps[0].actionType)
    assertEquals("15", plan.steps[0].params["durationMinutes"])
  }
}
