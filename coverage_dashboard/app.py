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

DEVIN_SESSIONS = [
    {
        "label": "Session 1 — Transaction-Service coverage",
        "url": "https://app.devin.ai/sessions/3ad1d2a3421144139599e0b04ddbcbf8?tab=pr%3A2",
        "pr": "PR #2",
    },
    {
        "label": "Session 2 — Transaction-Service coverage",
        "url": "https://app.devin.ai/sessions/e59b1bd04e304ba5b55eaffa48a91516?tab=pr%3A3",
        "pr": "PR #3",
    },
]

# ── Colour palette ───────────────────────────────────────────────────────────

BG = "#09090b"
CARD_BG = "#18181b"
CARD_BORDER = "#27272a"
GREEN = "#22c55e"
GREEN_DIM = "#166534"
TEXT = "#fafafa"
TEXT_SECONDARY = "#a1a1aa"
TEXT_MUTED = "#71717a"
TAG_BG = "#27272a"
TAG_TEXT = "#d4d4d8"
BAR_TRACK = "#27272a"

# ── Helpers ──────────────────────────────────────────────────────────────────


def _pct(value):
    return f"{value}%"


def _progress_bar(pct, colour=GREEN, height="4px", track=BAR_TRACK):
    return html.Div(
        html.Div(
            style={
                "width": f"{pct}%",
                "height": height,
                "backgroundColor": colour,
                "borderRadius": "2px",
                "transition": "width 0.4s ease",
            },
        ),
        style={
            "width": "100%",
            "height": height,
            "backgroundColor": track,
            "borderRadius": "2px",
            "overflow": "hidden",
        },
    )


def _card_style(**overrides):
    base = {
        "backgroundColor": CARD_BG,
        "border": f"1px solid {CARD_BORDER}",
        "borderRadius": "8px",
        "padding": "16px 20px",
    }
    base.update(overrides)
    return base


# ── Layout builders ──────────────────────────────────────────────────────────


def _badge(text, bg=GREEN_DIM, color=GREEN):
    return html.Span(
        text,
        style={
            "backgroundColor": bg,
            "color": color,
            "padding": "3px 10px",
            "borderRadius": "4px",
            "fontSize": "12px",
            "fontWeight": "500",
        },
    )


def _section_label(text):
    return html.Div(
        text,
        style={
            "color": TEXT_MUTED,
            "fontSize": "11px",
            "letterSpacing": "0.05em",
            "textTransform": "uppercase",
            "marginBottom": "12px",
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
                            "color": TEXT,
                            "fontSize": "22px",
                            "fontWeight": "600",
                            "margin": "0",
                            "letterSpacing": "-0.02em",
                        },
                    ),
                    html.P(
                        "Transaction-Service  /  OCC Examination Readiness",
                        style={
                            "color": TEXT_MUTED,
                            "fontSize": "13px",
                            "margin": "2px 0 0 0",
                        },
                    ),
                ],
            ),
            _badge("PR open"),
        ],
        style={
            "display": "flex",
            "justifyContent": "space-between",
            "alignItems": "center",
            "padding": "20px 0 16px 0",
        },
    )


def _metric_card(card_id, label):
    return html.Div(
        [
            html.Div(
                "—",
                id=card_id,
                style={
                    "fontSize": "28px",
                    "fontWeight": "600",
                    "color": TEXT,
                    "lineHeight": "1",
                    "letterSpacing": "-0.02em",
                },
            ),
            html.Div(
                label,
                style={
                    "fontSize": "11px",
                    "color": TEXT_MUTED,
                    "marginTop": "6px",
                    "textTransform": "uppercase",
                    "letterSpacing": "0.04em",
                },
            ),
        ],
        style=_card_style(flex="1", minWidth="160px"),
    )


def _metric_row():
    return html.Div(
        [
            _metric_card("metric-coverage", "Overall coverage"),
            _metric_card("metric-compliance", "Compliance paths"),
            _metric_card("metric-tests", "Tests written"),
            _metric_card("metric-time", "Session time"),
        ],
        style={
            "display": "flex",
            "gap": "12px",
            "marginBottom": "28px",
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
                    "padding": "6px 16px",
                    "border": f"1px solid {CARD_BORDER}",
                    "borderRadius": "6px 0 0 6px",
                    "cursor": "pointer",
                    "fontSize": "12px",
                    "fontWeight": "500",
                    "backgroundColor": "transparent",
                    "color": TEXT_MUTED,
                    "outline": "none",
                },
            ),
            html.Button(
                "After Devin",
                id="btn-after",
                n_clicks=0,
                style={
                    "padding": "6px 16px",
                    "border": f"1px solid {GREEN_DIM}",
                    "borderRadius": "0 6px 6px 0",
                    "cursor": "pointer",
                    "fontSize": "12px",
                    "fontWeight": "500",
                    "backgroundColor": GREEN_DIM,
                    "color": GREEN,
                    "outline": "none",
                },
            ),
        ],
        style={"display": "flex", "marginBottom": "20px"},
    )


def _class_card(name):
    return html.Div(
        [
            html.Div(
                [
                    html.Div(
                        name,
                        style={
                            "fontWeight": "500",
                            "fontSize": "13px",
                            "color": TEXT,
                        },
                    ),
                    html.Div(
                        [
                            html.Span(
                                "—",
                                id=f"class-pct-{name}",
                                style={
                                    "fontSize": "22px",
                                    "fontWeight": "600",
                                    "color": GREEN,
                                    "letterSpacing": "-0.02em",
                                },
                            ),
                            html.Span(
                                "",
                                id=f"class-tests-{name}",
                                style={
                                    "fontSize": "11px",
                                    "color": TEXT_MUTED,
                                    "marginLeft": "8px",
                                },
                            ),
                        ],
                        style={
                            "display": "flex",
                            "alignItems": "baseline",
                            "marginTop": "8px",
                        },
                    ),
                ],
            ),
            html.Div(
                id=f"class-bar-{name}",
                style={"marginTop": "10px"},
            ),
        ],
        style=_card_style(**{
            "flex": "1 1 calc(50% - 6px)",
            "minWidth": "240px",
        }),
    )


def _coverage_section():
    class_names = list(AFTER_DATA["classes"].keys())
    return html.Div(
        [
            _section_label("Coverage by compliance-critical class"),
            html.Div(
                [_class_card(n) for n in class_names],
                style={
                    "display": "flex",
                    "gap": "12px",
                    "flexWrap": "wrap",
                },
            ),
        ],
        style={"marginBottom": "32px"},
    )


def _tag(text):
    return html.Span(
        text,
        style={
            "backgroundColor": TAG_BG,
            "color": TAG_TEXT,
            "padding": "2px 8px",
            "borderRadius": "4px",
            "fontSize": "11px",
            "fontWeight": "400",
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
                        [
                            html.Span(
                                item["service"],
                                style={
                                    "fontWeight": "500",
                                    "fontSize": "13px",
                                    "color": TEXT,
                                },
                            ),
                        ],
                    ),
                    html.Div(
                        [_tag(t) for t in item["tags"]],
                        style={
                            "display": "flex",
                            "gap": "4px",
                            "marginTop": "6px",
                            "flexWrap": "wrap",
                        },
                    ),
                ],
            ),
            html.Div(
                _tag(item["phase"]),
                style={"position": "absolute", "top": "14px", "right": "14px"},
            ),
            html.Div(
                [
                    html.Div(
                        [
                            html.Span(
                                "Before ",
                                style={"fontSize": "11px", "color": TEXT_MUTED},
                            ),
                            html.Span(
                                _pct(before_pct),
                                style={
                                    "fontSize": "11px",
                                    "fontWeight": "500",
                                    "color": TEXT_SECONDARY,
                                },
                            ),
                        ],
                        style={"marginBottom": "4px"},
                    ),
                    _progress_bar(before_pct, colour=TEXT_MUTED),
                    html.Div(
                        [
                            html.Span(
                                f"{label_after} ",
                                style={"fontSize": "11px", "color": TEXT_MUTED},
                            ),
                            html.Span(
                                _pct(after_pct),
                                style={
                                    "fontSize": "11px",
                                    "fontWeight": "500",
                                    "color": GREEN,
                                },
                            ),
                        ],
                        style={"marginTop": "8px", "marginBottom": "4px"},
                    ),
                    _progress_bar(after_pct, colour=GREEN),
                ],
                style={"marginTop": "14px"},
            ),
        ],
        style={
            **_card_style(),
            "position": "relative",
            "flex": "1 1 calc(50% - 6px)",
            "minWidth": "240px",
        },
    )


def _roadmap_section():
    return html.Div(
        [
            _section_label("What this unlocks — roadmap"),
            html.Div(
                [_roadmap_card(item) for item in ROADMAP],
                style={
                    "display": "flex",
                    "gap": "12px",
                    "flexWrap": "wrap",
                },
            ),
        ],
        style={"marginBottom": "32px"},
    )


def _pr_card():
    return html.Div(
        [
            html.Div(
                [
                    html.Div(
                        "feat: add OCC compliance test coverage — transaction service",
                        style={
                            "fontWeight": "500",
                            "fontSize": "13px",
                            "color": TEXT,
                        },
                    ),
                    html.Div(
                        [_tag(f) for f in PR_FILES],
                        style={
                            "display": "flex",
                            "gap": "4px",
                            "marginTop": "8px",
                            "flexWrap": "wrap",
                        },
                    ),
                ],
                style={"flex": "1"},
            ),
            _badge("42 tests passing"),
        ],
        style={
            **_card_style(),
            "display": "flex",
            "justifyContent": "space-between",
            "alignItems": "center",
            "gap": "16px",
            "flexWrap": "wrap",
        },
    )


def _sessions_section():
    items = []
    for s in DEVIN_SESSIONS:
        items.append(
            html.A(
                html.Div(
                    [
                        html.Div(
                            [
                                html.Span(
                                    s["label"],
                                    style={
                                        "fontWeight": "500",
                                        "fontSize": "13px",
                                        "color": TEXT,
                                    },
                                ),
                                _tag(s["pr"]),
                            ],
                            style={
                                "display": "flex",
                                "justifyContent": "space-between",
                                "alignItems": "center",
                                "width": "100%",
                            },
                        ),
                        html.Div(
                            s["url"].split("?")[0],
                            style={
                                "fontSize": "11px",
                                "color": TEXT_MUTED,
                                "marginTop": "4px",
                                "overflow": "hidden",
                                "textOverflow": "ellipsis",
                            },
                        ),
                    ],
                    style=_card_style(),
                ),
                href=s["url"],
                target="_blank",
                style={"textDecoration": "none", "flex": "1 1 calc(50% - 6px)", "minWidth": "240px"},
            )
        )
    return html.Div(
        [
            _section_label("Devin sessions"),
            html.Div(
                items,
                style={"display": "flex", "gap": "12px", "flexWrap": "wrap"},
            ),
        ],
        style={"marginTop": "16px"},
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
                _sessions_section(),
            ],
            style={
                "maxWidth": "960px",
                "margin": "0 auto",
                "padding": "0 24px 40px 24px",
            },
        ),
    ],
    style={
        "backgroundColor": BG,
        "minHeight": "100vh",
        "fontFamily": "-apple-system, BlinkMacSystemFont, 'Segoe UI', 'Inter', "
                      "system-ui, sans-serif",
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

    base = {
        "padding": "6px 16px",
        "cursor": "pointer",
        "fontSize": "12px",
        "fontWeight": "500",
        "outline": "none",
    }
    base_before = {**base, "borderRadius": "6px 0 0 6px"}
    base_after = {**base, "borderRadius": "0 6px 6px 0"}

    if "btn-before" in triggered:
        return (
            "before",
            {**base_before, "backgroundColor": CARD_BORDER, "color": TEXT,
             "border": f"1px solid {TEXT_MUTED}"},
            {**base_after, "backgroundColor": "transparent", "color": TEXT_MUTED,
             "border": f"1px solid {CARD_BORDER}"},
        )
    return (
        "after",
        {**base_before, "backgroundColor": "transparent", "color": TEXT_MUTED,
         "border": f"1px solid {CARD_BORDER}"},
        {**base_after, "backgroundColor": GREEN_DIM, "color": GREEN,
         "border": f"1px solid {GREEN_DIM}"},
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
        colour = GREEN if pct >= 85 else ("#eab308" if pct >= 50 else TEXT_MUTED)
        results.extend(
            [
                _pct(pct),
                {
                    "fontSize": "22px",
                    "fontWeight": "600",
                    "color": colour if pct > 0 else TEXT_MUTED,
                    "letterSpacing": "-0.02em",
                },
                f"{tests} tests" if tests else "0 tests",
                _progress_bar(pct, colour=colour if pct > 0 else BAR_TRACK),
            ]
        )
    return results


# ── Entry-point ──────────────────────────────────────────────────────────────

if __name__ == "__main__":
    app.run(debug=False, host="0.0.0.0", port=8050)
