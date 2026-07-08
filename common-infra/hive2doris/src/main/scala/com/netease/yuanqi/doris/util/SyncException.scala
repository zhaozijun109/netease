package com.netease.yuanqi.doris.util

// ─── Sync Exception Hierarchy ─────────────────────────────────────────────────

/**
 * Base exception for all sync-related errors.
 *
 * @param message  human-readable error description
 * @param cause    underlying exception (if any)
 */
sealed abstract class SyncException(
  message: String,
  cause: Throwable = null
) extends Exception(message, cause) {

  /** The table associated with this error. */
  def table: String
}

// ─── Schema Exceptions ───────────────────────────────────────────────────────

/**
 * Thrown when the Doris table schema is incompatible and requires an ALTER TABLE
 * or a full table rebuild (e.g. key column changes).
 *
 * Recovery: light changes → ALTER TABLE; key column changes → drop & recreate.
 */
case class SchemaException(
  table: String,
  detail: String,
  requiresRebuild: Boolean = false,
  cause: Throwable = null
) extends SyncException(
  s"Schema mismatch for table '$table': $detail" +
    (if (requiresRebuild) " [requires rebuild]" else " [light schema change]"),
  cause
)
