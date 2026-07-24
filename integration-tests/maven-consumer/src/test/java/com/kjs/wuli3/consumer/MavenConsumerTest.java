package com.kjs.wuli3.consumer;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class MavenConsumerTest {

    @Test
    void consumesThePublishedEventArtifactsWithoutTheJavaClientPreviewDependency() {
        assertFalse(MavenConsumer.createIdentifier().isBlank());
    }
}
