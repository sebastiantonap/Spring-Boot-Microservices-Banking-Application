"""
Coverage Dashboard – Transaction-Service (OCC Compliance)
=========================================================
Single-file Dash app that visualises notional test-coverage data for the
Spring-Boot-Microservices-Banking-Application Transaction-Service.

Run:  python app.py
"""

import dash
from dash import html, dcc, Input, Output, State
import dash_bootstrap_components as dbc

# ── Notional data ────────────────────────────────────────────────────────────

BEFORE_DATA = {
    "overall_coverage": 0,
    "compliance_paths": 0,
    "tests_written": 0,
    "session_time": "—",
    "classes": {
        "AuthService": {"coverage": 0, "tests": 0},
        "TransactionProcessor": {"coverage": 0, "tests": 0},
        "AuditLogger": {"coverage": 0, "tests": 0},
        "PIIDataHandler": {"coverage": 0, "tests": 0},
    },
}

AFTER_DATA = {
    "overall_coverage": 87,
    "compliance_paths": 91,
    "tests_written": 42,
    "session_time": "~8 min",
    "classes": {
        "AuthService": {"coverage": 94, "tests": 11},
        "TransactionProcessor": {"coverage": 89, "tests": 14},
        "AuditLogger": {"coverage": 88, "tests": 9},
        "PIIDataHandler": {"coverage": 96, "tests": 8},
    },
}

ROADMAP = [
    {
        "service": "Transaction service (pilot)",
        "tags": ["Java", "Spring Boot"],
        "phase": "Phase 1",
        "before": 0,
        "after": 87,
    },
    {
        "service": "Auth service",
        "tags": ["Java", "SSO/MFA"],
        "phase": "Phase 1 next",
        "before": 22,
        "target": 88,
    },
    {
        "service": "Notification service",
        "tags": ["Java", "IBM MQ"],
        "phase": "Phase 2 prereq",
        "before": 18,
        "target": 85,
    },
    {
        "service": "10 remaining services",
        "tags": ["Java/TypeScript/Python", "MultiDevin"],
        "phase": "MultiDevin",
        "before": 30,
        "target": 85,
    },
]

PR_FILES = [
    "AuthServiceTest.java",
    "TransactionProcessorTest.java",
    "AuditLoggerTest.java",
    "PIIDataHandlerTest.java",
]

# ── Colour palette ───────────────────────────────────────────────────────────

BG = "#0a0a0a"
CARD_BG = "#f5f4f0"
GREEN = "#2d7a3a"
GREEN_LIGHT = "#4caf50"
BADGE_GREEN = "#22c55e"
TEXT_DARK = "#1a1a1a"
TEXT_MUTED = "#6b7280"
TAG_BG = "#e5e7eb"
BAR_TRACK = "#e0e0e0"

# ── Class icons (Unicode) ───────────────────────────────────────────────────

CLASS_ICONS = {
    "AuthService": "🔐",
    "TransactionProcessor": "💳",
    "AuditLogger": "📋",
    "PIIDataHandler": "🛡️",
}

CLASS_DESCRIPTIONS = {
    "AuthService": "JWT validation & session management",
    "TransactionProcessor": "Core DEBIT / CREDIT engine",
    "AuditLogger": "Immutable event recorder",
    "PIIDataHandler": "PII masking & validation",
}

# ── Helpers ──────────────────────────────────────────────────────────────────


def _pct(value):
    return f"{value}%"


def _progress_bar(pct, colour=GREEN_LIGHT, height="8px", track=BAR_TRACK):
    return html.Div(
        html.Div(
            style={
                "width": f"{pct}%",
                "height": height,
                "backgroundColor": colour,
                "borderRadius": "4px",
                "transition": "width 0.5s ease",
            },
        ),
        style={
            "width": "100%",
            "height": height,
            "backgroundColor": track,
            "borderRadius": "4px",
            "overflow": "hidden",
        },
    )


# ── Layout builders ──────────────────────────────────────────────────────────


def _badge(text, bg=BADGE_GREEN):
    return html.Span(
        text,
        style={
            "backgroundColor": bg,
            "color": "white",
            "padding": "4px 14px",
            "borderRadius": "20px",
            "fontSize": "13px",
            "fontWeight": "600",
            "letterSpacing": "0.5px",
        },
    )


def _header():
    return html.Div(
        [
            html.Div(
                [
                    html.H1(
                        "Compliance Test Coverage",
                        style={
                            "color": "white",
                            "fontSize": "28px",
                            "fontWeight": "700",
                            "margin": "0",
                        },
                    ),
                    html.P(
                        "Transaction-Service · OCC Examination Readiness",
                        style={
                            "color": TEXT_MUTED,
                            "fontSize": "14px",
                            "margin": "4px 0 0 0",
                        },
                    ),
                ],
            ),
            html.Div(
                [
                    _badge("PR open"),
                ],
                style={"display": "flex", "alignItems": "center", "gap": "8px"},
            ),
        ],
        style={
            "display": "flex",
            "justifyContent": "space-between",
            "alignItems": "center",
            "padding": "24px 0",
        },
    )


def _metric_card(card_id, label, icon):
    return html.Div(
        [
            html.Div(
                icon,
                style={"fontSize": "20px", "marginBottom": "8px"},
            ),
            html.Div(
                "—",
                id=card_id,
                style={
                    "fontSize": "32px",
                    "fontWeight": "700",
                    "color": TEXT_DARK,
                    "lineHeight": "1.1",
                },
            ),
            html.Div(
                label,
                style={
                    "fontSize": "12px",
                    "color": TEXT_MUTED,
                    "marginTop": "4px",
                    "textTransform": "uppercase",
                    "letterSpacing": "0.5px",
                },
            ),
        ],
        style={
            "backgroundColor": CARD_BG,
            "borderRadius": "12px",
            "padding": "20px",
            "flex": "1",
            "minWidth": "180px",
        },
    )


def _metric_row():
    return html.Div(
        [
            _metric_card("metric-coverage", "Overall coverage", "📊"),
            _metric_card("metric-compliance", "Compliance paths", "✅"),
            _metric_card("metric-tests", "Tests written", "🧪"),
            _metric_card("metric-time", "Session time", "⏱️"),
        ],
        style={
            "display": "flex",
            "gap": "16px",
            "marginBottom": "32px",
            "flexWrap": "wrap",
        },
    )


def _toggle():
    return html.Div(
        [
            html.Button(
                "Before Devin",
                id="btn-before",
                n_clicks=0,
                style={
                    "padding": "8px 20px",
                    "border": "1px solid #444",
                    "borderRadius": "8px 0 0 8px",
                    "cursor": "pointer",
                    "fontSize": "13px",
                    "fontWeight": "600",
                    "backgroundColor": BG,
                    "color": "white",
                    "outline": "none",
                },
            ),
            html.Button(
                "After Devin",
                id="btn-after",
                n_clicks=0,
                style={
                    "padding": "8px 20px",
                    "border": "1px solid " + GREEN,
                    "borderRadius": "0 8px 8px 0",
                    "cursor": "pointer",
                    "fontSize": "13px",
                    "fontWeight": "600",
                    "backgroundColor": GREEN,
                    "color": "white",
                    "outline": "none",
                },
            ),
        ],
        style={
            "display": "flex",
            "marginBottom": "24px",
        },
    )


def _class_card(name):
    return html.Div(
        [
            html.Div(
                [
                    html.Span(
                        CLASS_ICONS.get(name, "📦"),
                        style={"fontSize": "28px"},
                    ),
                    html.Div(
                        [
                            html.Div(
                                name,
                                style={
                                    "fontWeight": "700",
                                    "fontSize": "15px",
                                    "color": TEXT_DARK,
                                },
                            ),
                            html.Div(
                                CLASS_DESCRIPTIONS.get(name, ""),
                                style={
                                    "fontSize": "12px",
                                    "color": TEXT_MUTED,
                                },
                            ),
                        ],
                    ),
                ],
                style={"display": "flex", "gap": "12px", "alignItems": "center"},
            ),
            html.Div(
                [
                    html.Span(
                        "—",
                        id=f"class-pct-{name}",
                        style={
                            "fontSize": "28px",
                            "fontWeight": "700",
                            "color": GREEN,
                        },
                    ),
                    html.Span(
                        "",
                        id=f"class-tests-{name}",
                        style={
                            "fontSize": "12px",
                            "color": TEXT_MUTED,
                            "marginLeft": "8px",
                        },
                    ),
                ],
                style={"display": "flex", "alignItems": "baseline"},
            ),
            html.Div(id=f"class-bar-{name}", style={"marginTop": "8px"}),
        ],
        style={
            "backgroundColor": CARD_BG,
            "borderRadius": "12px",
            "padding": "20px",
            "flex": "1 1 calc(50% - 8px)",
            "minWidth": "260px",
        },
    )


def _coverage_section():
    class_names = list(AFTER_DATA["classes"].keys())
    return html.Div(
        [
            html.Div(
                "COVERAGE BY COMPLIANCE-CRITICAL CLASS",
                style={
                    "color": TEXT_MUTED,
                    "fontSize": "12px",
                    "letterSpacing": "1px",
                    "marginBottom": "16px",
                    "fontWeight": "600",
                },
            ),
            html.Div(
                [_class_card(n) for n in class_names],
                style={
                    "display": "flex",
                    "gap": "16px",
                    "flexWrap": "wrap",
                },
            ),
        ],
        style={"marginBottom": "40px"},
    )


def _tag(text):
    return html.Span(
        text,
        style={
            "backgroundColor": TAG_BG,
            "color": TEXT_DARK,
            "padding": "2px 10px",
            "borderRadius": "12px",
            "fontSize": "11px",
            "fontWeight": "500",
        },
    )


def _roadmap_card(item):
    before_pct = item["before"]
    after_pct = item.get("after", item.get("target", 0))
    label_after = "After" if "after" in item else "Target"
    return html.Div(
        [
            html.Div(
                [
                    html.Div(
                        item["service"],
                        style={
                            "fontWeight": "700",
                            "fontSize": "15px",
                            "color": TEXT_DARK,
                        },
                    ),
                    html.Div(
                        [_tag(t) for t in item["tags"]],
                        style={
                            "display": "flex",
                            "gap": "6px",
                            "marginTop": "6px",
                            "flexWrap": "wrap",
                        },
                    ),
                ],
            ),
            html.Div(
                _tag(item["phase"]),
                style={"position": "absolute", "top": "16px", "right": "16px"},
            ),
            html.Div(
                [
                    html.Div(
                        [
                            html.Span(
                                "Before ",
                                style={"fontSize": "12px", "color": TEXT_MUTED},
                            ),
                            html.Span(
                                _pct(before_pct),
                                style={
                                    "fontSize": "12px",
                                    "fontWeight": "600",
                                    "color": TEXT_DARK,
                                },
                            ),
                        ],
                        style={"marginBottom": "4px"},
                    ),
                    _progress_bar(before_pct, colour="#9ca3af"),
                    html.Div(
                        [
                            html.Span(
                                f"{label_after} ",
                                style={"fontSize": "12px", "color": TEXT_MUTED},
                            ),
                            html.Span(
                                _pct(after_pct),
                                style={
                                    "fontSize": "12px",
                                    "fontWeight": "600",
                                    "color": GREEN,
                                },
                            ),
                        ],
                        style={"marginTop": "10px", "marginBottom": "4px"},
                    ),
                    _progress_bar(after_pct, colour=GREEN_LIGHT),
                ],
                style={"marginTop": "16px"},
            ),
        ],
        style={
            "backgroundColor": CARD_BG,
            "borderRadius": "12px",
            "padding": "20px",
            "position": "relative",
            "flex": "1 1 calc(50% - 8px)",
            "minWidth": "260px",
        },
    )


def _roadmap_section():
    return html.Div(
        [
            html.Div(
                "WHAT THIS UNLOCKS — ROADMAP",
                style={
                    "color": TEXT_MUTED,
                    "fontSize": "12px",
                    "letterSpacing": "1px",
                    "marginBottom": "16px",
                    "fontWeight": "600",
                },
            ),
            html.Div(
                [_roadmap_card(item) for item in ROADMAP],
                style={
                    "display": "flex",
                    "gap": "16px",
                    "flexWrap": "wrap",
                },
            ),
        ],
        style={"marginBottom": "40px"},
    )


def _pr_card():
    return html.Div(
        [
            html.Div(
                [
                    html.Div(
                        [
                            html.Span(
                                "🔀",
                                style={"fontSize": "18px", "marginRight": "8px"},
                            ),
                            html.Span(
                                "feat: add OCC compliance test coverage — transaction service",
                                style={
                                    "fontWeight": "700",
                                    "fontSize": "15px",
                                    "color": TEXT_DARK,
                                },
                            ),
                        ],
                        style={"display": "flex", "alignItems": "center"},
                    ),
                    html.Div(
                        [_tag(f) for f in PR_FILES],
                        style={
                            "display": "flex",
                            "gap": "6px",
                            "marginTop": "12px",
                            "flexWrap": "wrap",
                        },
                    ),
                ],
                style={"flex": "1"},
            ),
            html.Div(
                [
                    _badge("42 tests passing", bg=GREEN),
                ],
                style={
                    "display": "flex",
                    "alignItems": "center",
                    "flexShrink": "0",
                },
            ),
        ],
        style={
            "backgroundColor": CARD_BG,
            "borderRadius": "12px",
            "padding": "20px",
            "display": "flex",
            "justifyContent": "space-between",
            "alignItems": "center",
            "gap": "20px",
            "flexWrap": "wrap",
        },
    )


# ── App ──────────────────────────────────────────────────────────────────────

app = dash.Dash(
    __name__,
    external_stylesheets=[dbc.themes.BOOTSTRAP],
    title="Coverage Dashboard — Transaction-Service",
)

app.layout = html.Div(
    [
        dcc.Store(id="view-store", data="after"),
        html.Div(
            [
                _header(),
                _toggle(),
                _metric_row(),
                _coverage_section(),
                _roadmap_section(),
                _pr_card(),
            ],
            style={
                "maxWidth": "1100px",
                "margin": "0 auto",
                "padding": "0 24px 48px 24px",
            },
        ),
    ],
    style={
        "backgroundColor": BG,
        "minHeight": "100vh",
        "fontFamily": "'Inter', 'Segoe UI', system-ui, -apple-system, sans-serif",
    },
)

# ── Callbacks ────────────────────────────────────────────────────────────────


@app.callback(
    Output("view-store", "data"),
    Output("btn-before", "style"),
    Output("btn-after", "style"),
    Input("btn-before", "n_clicks"),
    Input("btn-after", "n_clicks"),
)
def toggle_view(n_before, n_after):
    ctx = dash.callback_context
    triggered = ctx.triggered[0]["prop_id"] if ctx.triggered else "btn-after.n_clicks"

    base_before = {
        "padding": "8px 20px",
        "borderRadius": "8px 0 0 8px",
        "cursor": "pointer",
        "fontSize": "13px",
        "fontWeight": "600",
        "outline": "none",
    }
    base_after = {
        "padding": "8px 20px",
        "borderRadius": "0 8px 8px 0",
        "cursor": "pointer",
        "fontSize": "13px",
        "fontWeight": "600",
        "outline": "none",
    }

    if "btn-before" in triggered:
        return (
            "before",
            {**base_before, "backgroundColor": "#333", "color": "white",
             "border": "1px solid #555"},
            {**base_after, "backgroundColor": BG, "color": TEXT_MUTED,
             "border": "1px solid #444"},
        )
    return (
        "after",
        {**base_before, "backgroundColor": BG, "color": TEXT_MUTED,
         "border": "1px solid #444"},
        {**base_after, "backgroundColor": GREEN, "color": "white",
         "border": "1px solid " + GREEN},
    )


@app.callback(
    Output("metric-coverage", "children"),
    Output("metric-compliance", "children"),
    Output("metric-tests", "children"),
    Output("metric-time", "children"),
    Input("view-store", "data"),
)
def update_metrics(view):
    d = AFTER_DATA if view == "after" else BEFORE_DATA
    cov = _pct(d["overall_coverage"]) if d["overall_coverage"] else "0%"
    comp = _pct(d["compliance_paths"]) if d["compliance_paths"] else "0%"
    tests = str(d["tests_written"]) if d["tests_written"] else "0"
    return cov, comp, tests, d["session_time"]


def _class_outputs(name):
    return [
        Output(f"class-pct-{name}", "children"),
        Output(f"class-pct-{name}", "style"),
        Output(f"class-tests-{name}", "children"),
        Output(f"class-bar-{name}", "children"),
    ]


# Build a single callback that updates all four class cards at once
_all_class_outputs = []
for _cn in AFTER_DATA["classes"]:
    _all_class_outputs.extend(_class_outputs(_cn))


@app.callback(
    _all_class_outputs,
    Input("view-store", "data"),
)
def update_classes(view):
    d = AFTER_DATA if view == "after" else BEFORE_DATA
    results = []
    for name in d["classes"]:
        info = d["classes"][name]
        pct = info["coverage"]
        tests = info["tests"]
        colour = GREEN if pct >= 85 else ("#f59e0b" if pct >= 50 else TEXT_MUTED)
        results.extend(
            [
                _pct(pct),
                {
                    "fontSize": "28px",
                    "fontWeight": "700",
                    "color": colour if pct > 0 else TEXT_MUTED,
                },
                f"{tests} tests" if tests else "0 tests",
                _progress_bar(pct, colour=colour if pct > 0 else BAR_TRACK),
            ]
        )
    return results


# ── Entry-point ──────────────────────────────────────────────────────────────

if __name__ == "__main__":
    app.run(debug=False, host="0.0.0.0", port=8050)
