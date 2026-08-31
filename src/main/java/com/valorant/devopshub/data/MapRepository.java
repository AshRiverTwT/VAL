package com.valorant.devopshub.data;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.valorant.devopshub.model.GameMap;

@Repository
public class MapRepository {

    private final List<GameMap> maps = List.of(
            new GameMap("bind", "Bind", "2-site, no rotation",
                    "A desert map with two bomb sites connected only by teleporters, forcing teams to " +
                            "commit early since there is no direct rotation path between sites.",
                    List.of("Site A", "Site B"), "#D9A066"),

            new GameMap("haven", "Haven", "3-site",
                    "The only map with three bomb sites, demanding wider map control and more flexible " +
                            "rotations from both attackers and defenders.",
                    List.of("Site A", "Site B", "Site C"), "#6B8FA3"),

            new GameMap("split", "Split", "2-site, vertical",
                    "A vertical map full of tight corridors and rope ascenders, where mid control heavily " +
                            "influences which site a team can safely attack.",
                    List.of("Site A", "Site B"), "#8A6BA3"),

            new GameMap("ascent", "Ascent", "2-site, Italian courtyard",
                    "An open Italian-inspired map with long sightlines and door-controlled chokepoints " +
                            "that reward good utility usage.",
                    List.of("Site A", "Site B"), "#A3936B"),

            new GameMap("icebox", "Icebox", "2-site, vertical",
                    "A snowy industrial site with zip lines and elevation changes, where sites are won " +
                            "and lost on verticality and crossfires.",
                    List.of("Site A", "Site B"), "#7FB3C7"),

            new GameMap("pearl", "Pearl", "2-site, underwater city",
                    "A submerged city map with a central mid area that controls access to both sites, " +
                            "rewarding coordinated executes.",
                    List.of("Site A", "Site B"), "#4A7A96")
    );

    public List<GameMap> findAll() {
        return maps;
    }
}
