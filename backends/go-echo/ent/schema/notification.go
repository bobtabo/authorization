package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema"
	"entgo.io/ent/schema/field"
	"time"
)

// Notification holds the schema definition for the Notification entity.
type Notification struct {
	ent.Schema
}

func (Notification) Annotations() []schema.Annotation {
	return []schema.Annotation{
		entsql.Annotation{Table: "notifications"},
	}
}

func (Notification) Fields() []ent.Field {
	return []ent.Field{
		field.Uint64("id").Positive().Immutable(),
		field.Uint("staff_id"),
		field.Int("message_type"),
		field.String("title"),
		field.String("message"),
		field.String("url").Optional().Nillable(),
		field.Bool("read").Default(false),
		field.Time("created_at").Default(time.Now),
		field.Uint("created_by").Optional().Nillable(),
		field.Time("updated_at").Default(time.Now),
		field.Uint("updated_by").Optional().Nillable(),
		field.Time("deleted_at").Optional().Nillable(),
		field.Uint("deleted_by").Optional().Nillable(),
		field.Int("version").Default(1),
	}
}

func (Notification) Edges() []ent.Edge {
	return nil
}
