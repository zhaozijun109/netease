package com.netease.yuanqi.doris.util

/**
 * Lightweight command-line argument parser.
 *
 * Supports `--key value` and `--flag` (boolean) style arguments.
 *
 * Usage:
 * {{{
 *   val args = Args(Array("--table", "user_info", "--full"))
 *   args.required("table")         // "user_info"
 *   args.optional("partitions")    // None
 *   args.boolean("full")           // true
 * }}}
 */
class Args private(argMap: Map[String, String]) {

  /** Get a required argument. Throws if missing. */
  def required(key: String): String =
    argMap.getOrElse(key, throw new IllegalArgumentException(s"Missing required argument: --$key"))

  /** Get an optional argument. */
  def optional(key: String): Option[String] = argMap.get(key)

  /** Check if a boolean flag is present. */
  def boolean(key: String): Boolean = argMap.contains(key)

  override def toString: String = argMap.map { case (k, v) => s"--$k=$v" }.mkString(" ")
}

object Args {
  /**
   * Parse `--key value` and `--flag` style arguments into a map.
   */
  def apply(args: Array[String]): Args = {
    val map = scala.collection.mutable.LinkedHashMap.empty[String, String]
    var i = 0
    while (i < args.length) {
      val arg = args(i)
      if (arg.startsWith("--")) {
        val key = arg.stripPrefix("--")
        // If next arg exists and is NOT a flag (does not start with --), treat it as the value
        if (i + 1 < args.length && !args(i + 1).startsWith("--")) {
          map(key) = args(i + 1)
          i += 2
        } else {
          // Boolean flag
          map(key) = "true"
          i += 1
        }
      } else {
        // Skip unknown positional args
        i += 1
      }
    }
    new Args(map.toMap)
  }
}