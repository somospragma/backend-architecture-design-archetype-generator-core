package com.pragma.archetype.property

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.PropertyTesting

/**
 * Base class for property-based tests using Kotest.
 * 
 * Property-based testing verifies that certain properties (invariants) hold
 * for a wide range of inputs, rather than testing specific examples.
 */
abstract class PropertyTestBase : StringSpec() {
    
    init {
        // Configure property testing
        PropertyTesting.defaultIterationCount = 100
        PropertyTesting.defaultShrinkingMode = io.kotest.property.ShrinkingMode.Bounded(1000)
    }
}
