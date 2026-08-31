package com.valorant.devopshub.data;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.valorant.devopshub.model.PatchNote;

/**
 * Sample patch note content for the /patch-notes page. This is illustrative
 * data written for this DevOps lab project - it is not real VALORANT patch
 * data and should be labeled as such in the UI.
 */
@Repository
public class PatchNoteRepository {

    private final List<PatchNote> patchNotes = List.of(
            new PatchNote("9.10", "2026-08-19",
                    List.of("Tactical Hub sample patch - agent balance pass", "Minor map callout adjustments"),
                    List.of("Killjoy: Turret detection cone slightly widened",
                            "Reyna: Devour heal amount reduced by 5%"),
                    List.of("Pearl: Fixed a sightline exploit near Site B link"),
                    List.of("Fixed a bug where ability icons could overlap on ultra-wide resolutions")),

            new PatchNote("9.09", "2026-08-05",
                    List.of("Sample economy tuning update", "Quality-of-life spectator improvements"),
                    List.of("Raze: Blast Pack self-damage slightly reduced",
                            "Sova: Recon Bolt cooldown increased by 5 seconds"),
                    List.of("Icebox: Adjusted collision on Site A crate stack"),
                    List.of("Fixed an issue where the round timer could desync in spectator mode")),

            new PatchNote("9.08", "2026-07-22",
                    List.of("Sample patch introducing this hub's demo data set"),
                    List.of("Initial balance baseline established for all sample agents"),
                    List.of("Initial map rotation for the sample data set finalized"),
                    List.of("N/A - baseline patch for demo purposes"))
    );

    public List<PatchNote> findAll() {
        return patchNotes;
    }
}
