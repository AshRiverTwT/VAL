package com.valorant.devopshub.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.valorant.devopshub.data.AgentRepository;
import com.valorant.devopshub.data.MapRepository;
import com.valorant.devopshub.data.PatchNoteRepository;
import com.valorant.devopshub.data.WeaponRepository;
import com.valorant.devopshub.model.Agent;
import com.valorant.devopshub.model.GameMap;
import com.valorant.devopshub.model.PatchNote;
import com.valorant.devopshub.model.Weapon;

/**
 * Read-only JSON content endpoints consumed by the static frontend
 * (src/main/resources/static/js/*.js). Keeping the frontend static and the
 * data behind small REST endpoints keeps the "static site" requirement
 * intact while still allowing content to be maintained in one place.
 */
@RestController
public class ContentApiController {

    private final AgentRepository agentRepository;
    private final MapRepository mapRepository;
    private final WeaponRepository weaponRepository;
    private final PatchNoteRepository patchNoteRepository;

    public ContentApiController(AgentRepository agentRepository,
                                 MapRepository mapRepository,
                                 WeaponRepository weaponRepository,
                                 PatchNoteRepository patchNoteRepository) {
        this.agentRepository = agentRepository;
        this.mapRepository = mapRepository;
        this.weaponRepository = weaponRepository;
        this.patchNoteRepository = patchNoteRepository;
    }

    @GetMapping("/api/agents")
    public List<Agent> getAgents() {
        return agentRepository.findAll();
    }

    @GetMapping("/api/maps")
    public List<GameMap> getMaps() {
        return mapRepository.findAll();
    }

    @GetMapping("/api/weapons")
    public List<Weapon> getWeapons() {
        return weaponRepository.findAll();
    }

    @GetMapping("/api/patch-notes")
    public List<PatchNote> getPatchNotes() {
        return patchNoteRepository.findAll();
    }
}
