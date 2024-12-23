package com.thatsoulyguy.invasion2.collider.handler;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public record CollisionResult(boolean intersects, @NotNull Vector3f resolution) { }