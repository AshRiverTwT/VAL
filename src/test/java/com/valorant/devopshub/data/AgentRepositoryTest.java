package com.valorant.devopshub.data;

import com.valorant.devopshub.model.Agent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for the agent data set - no Spring context needed, so this
 * runs almost instantly during `mvn test`.
 */
class AgentRepositoryTest {

    private final AgentRepository repository = new AgentRepository();

    @Test
    void containsAtLeastTenAgents() {
        List<Agent> agents = repository.findAll();
        assertTrue(agents.size() >= 10, "Expected at least 10 agents, found " + agents.size());
    }

    @Test
    void everyAgentHasAUniqueIdAndFourAbilities() {
        List<Agent> agents = repository.findAll();
        Set<String> ids = agents.stream().map(Agent::id).collect(Collectors.toSet());
        assertEquals(agents.size(), ids.size(), "Agent ids must be unique");

        for (Agent agent : agents) {
            assertEquals(4, agent.abilities().size(),
                    "Agent " + agent.name() + " should have exactly 4 abilities");
        }
    }
}
