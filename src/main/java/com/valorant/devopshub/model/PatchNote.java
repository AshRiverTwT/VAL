package com.valorant.devopshub.model;

import java.util.List;

/**
 * A single sample patch note entry for the /patch-notes page.
 * This is illustrative/sample content for the DevOps lab, not real
 * VALORANT patch data.
 */
public record PatchNote(
        String version,
        String date,
        List<String> highlights,
        List<String> agentUpdates,
        List<String> mapUpdates,
        List<String> bugFixes
) {
}
